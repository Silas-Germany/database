package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import kotlin.test.Test
import kotlin.test.assertEquals

class ComplexOrmDatabaseSchemaTest {

    val complexOrmDatabaseSchema: ComplexOrmDatabaseSchemaInterface = ComplexOrmDatabaseSchema()

    @Test fun tableNames() {
        val schemaTables = complexOrmDatabaseSchema.tables.filterValues { it == Model.SchemaTable::class }.keys
        assertEquals(setOf("schema_table", "schema_table_connected_entries"), schemaTables,
            "Correct table with connected many-to-many connected tables are shown")
    }

    @Test fun allTableNamesExistInOtherMaps() {
        complexOrmDatabaseSchema.tables.keys.forEach {
            complexOrmDatabaseSchema.dropTableCommands.getValue(it)
            complexOrmDatabaseSchema.createTableCommands.getValue(it)
        }
    }

    @Test fun schemaFileCorrectlyCreated() {
        var expect = "CREATE TABLE 'schema_table'(\n" +
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
                "      'connected_entry_id' INTEGER NOT NULL REFERENCES 'reference_class'('id') ON DELETE CASCADE\n" +
                "      );"
        assertEquals(expect, complexOrmDatabaseSchema.createTableCommands.getValue("schema_table"))
        expect = "CREATE TABLE 'schema_table_connected_entries'(\n" +
                "      'schema_table_id' INTEGER NOT NULL REFERENCES 'schema_table'(id) ON DELETE CASCADE,\n" +
                "      'reference_class_id' INTEGER NOT NULL REFERENCES 'reference_class'(id) ON DELETE CASCADE,\n" +
                "      PRIMARY KEY ('schema_table_id','reference_class_id')\n" +
                "      );"
        assertEquals(expect, complexOrmDatabaseSchema.createTableCommands.getValue("schema_table_connected_entries"))
    }
}