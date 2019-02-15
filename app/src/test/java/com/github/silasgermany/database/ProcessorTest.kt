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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
class ProcessorTest {

    /* todo: Should work like this:
    Everything in the all-tables interface (marked with @ComplexOrmAllTables) is a database table, if it inherits somehow from ComplexOrmTable
    Everything inheriting from this is also a database table with the same name
    Everything else is not a database table, but a structure, that a database table can inherit from
    Columns of inherited tables will just be saved, if they have the column themselves through the override keyword
    All columns have to be marked with "by initMap" (otherwise they can't be saved or read)
    Every database table needs the constructor: "(initMap: MutableMap<String, Any?>)" (and can have "= default")
    No column is allowed to be read, that is not first written to or read by the database (see readable columns)
    todo: Needs column annotation: Read and save always; read always; save always (runtime default value)
    todo: Check again @ComplexOrmIgnore tag and "fun get..." properties: Should be completely ignored!!
    todo: Tables from the all-tables interface should also be normal tables (able to read and write them)
     */

    @Test
    fun checkSchemaInformation() {
        assertFalse(ComplexOrmSchema.createTableCommands.any { it.isEmpty() }, "Check, that every create table command exist")
        assertFalse(ComplexOrmSchema.dropTableCommands.any { it.isEmpty() }, "Check, that every drop table command exist")
        assertFalse(ComplexOrmSchema.tableNames.any { it.contains("[A-Z]".toRegex()) }, "Table names are not allowed to have upper case letter")
        assertEquals<Collection<KClass<*>>>(AllTables::class.nestedClasses.filter { it.isSubclassOf(ComplexOrmTable::class) }
            , ComplexOrmSchema.tables, "This variable should list all table classes")
    }

    fun checkSimpleColumns() {
        assertTrue(ComplexOrmTables.constructors.isNotEmpty(), "Constructors should exist")
    }
}