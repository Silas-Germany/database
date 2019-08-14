package com.github.silasgermany.complexorm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ComplexOrmInitializerTest {

    @Test fun schemaFileCorrectlyCreated() {
        val expect = "CREATE TABLE 'schema_test'(\n" +
                "      'id' BLOB NOT NULL PRIMARY KEY,\n" +
                "      'boolean' INTEGER NOT NULL DEFAULT 0,\n" +
                "      'int' INTEGER NOT NULL DEFAULT 1,\n" +
                "      'long' INTEGER NOT NULL DEFAULT 2,\n" +
                "      'float' REAL NOT NULL DEFAULT 3.1,\n" +
                "      'string' TEXT NOT NULL DEFAULT 'testDefault',\n" +
                "      'date' INTEGER NOT NULL DEFAULT '2019-01-01',\n" +
                "      'date_time' TEXT NOT NULL DEFAULT 1565762054,\n" +
                "      'byte_array' BLOB NOT NULL,\n" +
                "      'nullable_string222' TEXT\n" +
                "      );"
        assertEquals(expect, ComplexOrmDatabaseSchema().createTableCommands["schema_test"])
        fail("all working")
    }
}