package com.github.silasgermany.database

import com.github.silasgermany.complexorm.ComplexOrmSchema
import com.github.silasgermany.complexorm.ComplexOrmTables
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.database.tables.AllTables
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.test.*

@RunWith(JUnit4::class)
class ProcessorTest {

    /* todo: Should work like this:
    Everything in the all-tables interface (marked with @ComplexOrmAllTables) is a database table, if it inherits somehow from ComplexOrmTable
    Everything inheriting from this is also a database table with the same name
    Everything else inheriting from ComplexOrmTable is not a database table, but a structure, that a database table can inherit from
    Columns of inherited tables will just be read, if they have the column themselves through the override keyword or are marked by @ReadAlways
    All columns have to be marked with "by initMap" (otherwise they can't be saved or read)
    Nothing else is allowed to be delegated by a map (is criteria to distinguish between other values)
    Every database table needs the primary constructor: "(initMap: MutableMap<String, Any?>)" (and can have "= default" in case a new entry needs to be created
    or "constructor(id: Long): this(init(id))" if it should reference to another table with just the ID of the entry given)
    No column is allowed to be read, that is not first written to or read by the database (see readable columns) - careful: error on runtime!!!
     */

    @Test
    fun checkCreateTable() = assertFalse(
        ComplexOrmSchema.createTableCommands.any { it.isEmpty() },
        "Check, that every create table command exist"
    )

    @Test
    fun checkDropTable() = assertFalse(
        ComplexOrmSchema.dropTableCommands.any { it.isEmpty() },
        "Check, that every drop table command exist"
    )

    @Test
    fun checkTableNames() = assertFalse(
        ComplexOrmSchema.tables.values.any { it.contains("[A-Z]".toRegex()) },
        "Table names are not allowed to have upper case letter"
    )

    @Test
    fun checkTableClasses() = assertEquals<Collection<KClass<*>>>(
        AllTables::class.nestedClasses.filter { it.isSubclassOf(ComplexOrmTable::class) },
        ComplexOrmSchema.tables.keys,
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
        assertTrue(ComplexOrmTables.constructors.isNotEmpty(), "Constructors should exist")
        val allTablesNormalColumnsTables = ComplexOrmTables.normalColumns["AllTables"]
        assertNotNull(allTablesNormalColumnsTables, "Interface should exist (has table with normal columns)")
        assertNotNull(allTablesNormalColumnsTables.get("normal_table"), "Table should exist (has normal columns)")
        assertNull(allTablesNormalColumnsTables.get("empty_table"), "Table should not exist (no normal columns)")
        assertNull(allTablesNormalColumnsTables.get("no_table"), "Table should not exist (is not a table)")
        assertNotNull(
            allTablesNormalColumnsTables.get("indirect_table"),
            "Table should exist (has indirectly normal columns)"
        )
        assertEquals(
            setOf("inheriting_value"), allTablesNormalColumnsTables.get("double_indirect_table")?.keys,
            "Table should have indirect column"
        )
        assertEquals(
            setOf("middle_inheriting_value", "inheriting_value"),
            allTablesNormalColumnsTables.get("double_indirect_table")?.keys,
            "Table should have both indirect columns"
        )
    }
}