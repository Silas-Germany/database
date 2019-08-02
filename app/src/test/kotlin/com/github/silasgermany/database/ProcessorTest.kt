package com.github.silasgermany.database

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.database.tables.AllTables
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.*

@RunWith(JUnit4::class)
class ProcessorTest {

    private val complexOrmSchema by lazy {
        Class.forName("app.rev79.projects.utils.ComplexOrmDatabaseSchema")
            .getDeclaredField("INSTANCE").get(null) as ComplexOrmDatabaseSchemaInterface
    }

    private val complexOrmTables by lazy {
        Class.forName("app.rev79.projects.utils.ComplexOrmTableInfo")
            .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface
    }

    @Test
    fun checkCreateTable() = assertFalse(
        complexOrmSchema.createTableCommands.any { it.value.isEmpty() },
        "Check, that every create table command exist"
    )

    @Test
    fun checkDropTable() = assertFalse(
        complexOrmSchema.dropTableCommands.any { it.value.isEmpty() },
        "Check, that every drop table command exist"
    )

    @Test
    fun checkTableNames() = assertFalse(
        complexOrmSchema.tables.values.any { it.contains("[A-Z]".toRegex()) },
        "Table names are not allowed to have upper case letter"
    )

    @Test
    fun checkTableClasses() = assertEquals<Collection<Class<*>>>(
        AllTables::class.java.classes.filter { ComplexOrmTable::class.java.isAssignableFrom(it) }.toSet(),
        complexOrmSchema.tables.keys.mapTo(mutableSetOf()) { it.java },
        "This variable should list exactly all table classes"
    )

    @Test
    fun checkSpecialTableEntryCreation() = assertEquals<Map<String, Any?>>(
        mapOf("id" to 1L),
        ComplexOrmTable.create<AllTables.NormalTable>(1).map,
        "Init of tables should work"
    )

    @Test
    fun checkTableEntryCreation() =
        assertEquals<Map<String, Any?>>(mapOf("id" to 1L), AllTables.NormalTable(1).map, "Init of tables should work")

    @Test
    fun checkNormalColumnsOfDeclaredTables() {
        assertTrue(complexOrmTables.tableConstructors.isNotEmpty(), "Constructors should exist")
        val allTablesNormalColumnsTables = complexOrmTables.normalColumns
        assertNotNull(allTablesNormalColumnsTables, "Interface should exist (has table with normal columns)")
        assertNotNull(allTablesNormalColumnsTables[AllTables.NormalTable::class.java.canonicalName!!], "Table should exist (has normal columns)")
        assertNull(allTablesNormalColumnsTables[AllTables.EmptyTable::class.java.canonicalName!!], "Table should not exist (no normal columns)")
        assertNull(allTablesNormalColumnsTables[AllTables.NotATable::class.java.canonicalName!!], "Table should not exist (is not a table)")
        assertEquals(
            mutableSetOf("inheriting_value"), allTablesNormalColumnsTables[AllTables.IndirectTable::class.java.canonicalName!!]?.keys,
            "Table should have indirect column"
        )
        assertEquals(
            mutableSetOf("middle_inheriting_value", "inheriting_value", "direct_value"),
            allTablesNormalColumnsTables[AllTables.DoubleIndirectTable::class.java.canonicalName!!]?.keys,
            "Table should have both indirect columns"
        )
    }
}

/* Should work like this:
Everything in the all-tables interface (marked with @ComplexOrmAllTables) is a database table, if it inherits somehow from ComplexOrmTable
Everything inheriting from this is also a database table with the same name
Everything else inheriting from ComplexOrmTable is not a database table, but a structure, that a database table can inherit from
All columns of those tables are treated as columns from the inheriting table in the all-tables interface
Columns of inherited tables will just be read, if they have the column themselves (marked with the override keyword) or are marked by @ReadAlways
All columns have to be delegated with "by initMap" (otherwise they are not database columns)
Nothing else is allowed to be delegated by a map (this is the criteria to distinguish between other values)
Every database table needs the primary constructor: "(initMap: MutableMap<String, Any?>)" (and can have "= default" in case a new entry needs to be created
It can also have a secondary constructor with "constructor(id: Long): this(init(id))".
Through this a reference to another table can be given, where just the ID is known (sometimes necessary for saving)
No column is allowed to be read, that is not first written to or read by the database (see readable columns) - careful: error on runtime!!!
 */
