package com.github.silasgermany.complexorm.otherFunctions

import com.github.silasgermany.complexorm.Model
import com.github.silasgermany.complexorm.databaseSchema
import com.github.silasgermany.complexorm.helper.CommonHelper
import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import kotlin.test.Test
import kotlin.test.assertEquals

class ComplexOrmDatabaseSchemaTest: CommonHelper() {

    private val currentDatabaseSchema: ComplexOrmDatabaseSchemaInterface = databaseSchema

    @Test fun tableNames() {
        val schemaTables = currentDatabaseSchema.tables.filterValues { it == Model.ColumnTypesTable::class }.keys
        assertEquals(setOf("column_types_table", "column_types_table_connected_entries"), schemaTables,
            "Correct table with connected many-to-many connected tables are shown")
    }

    @Test fun allTableNamesExistInOtherMaps() {
        currentDatabaseSchema.tables.keys.forEach {
            currentDatabaseSchema.dropTableCommands.getValue(it)
            currentDatabaseSchema.createTableCommands.getValue(it)
        }
    }

    @Test fun schemaFileCorrectlyCreated() {
        var expect = "CREATE TABLE 'column_types_table'(\n" +
                "      'id' BLOB NOT NULL PRIMARY KEY,\n" +
                "      'boolean' INTEGER NOT NULL DEFAULT 0,\n" +
                "      'int' INTEGER NOT NULL DEFAULT 1,\n" +
                "      'long' INTEGER NOT NULL DEFAULT 2,\n" +
                "      'float' REAL NOT NULL DEFAULT 3.1,\n" +
                "      'string' TEXT NOT NULL DEFAULT 'testDefault',\n" +
                "      'date' INTEGER NOT NULL DEFAULT '2019-01-01',\n" +
                "      'date_time' TEXT NOT NULL DEFAULT 1565762054,\n" +
                "      'byte_array' BLOB NOT NULL,\n" +
                "      'nullable_entry' TEXT,\n" +
                "      'connected_entry_id' BLOB REFERENCES 'reference_table'('id') ON DELETE SET NULL\n" +
                "      );"
        assertEquals(expect, currentDatabaseSchema.createTableCommands.getValue("column_types_table"))
        expect = "CREATE TABLE\n" +
                "          'column_types_table_connected_entries'(\n" +
                "      'column_types_table_id' BLOB NOT NULL REFERENCES 'column_types_table'(id) ON DELETE CASCADE,\n" +
                "      'reference_table_id' BLOB NOT NULL REFERENCES 'reference_table'(id) ON DELETE CASCADE,\n" +
                "      PRIMARY KEY ('column_types_table_id','reference_table_id')\n" +
                "      );"
        assertEquals(expect, currentDatabaseSchema.createTableCommands.getValue("column_types_table_connected_entries"))
    }
}