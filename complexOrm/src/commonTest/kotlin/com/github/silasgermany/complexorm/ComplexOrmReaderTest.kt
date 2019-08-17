package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabase
import com.github.silasgermany.complexormapi.IdType
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ComplexOrmReaderTest {

    val testDatabase = ComplexOrmDatabase("/tmp/database.db")
    val complexOrmReader = ComplexOrmReader(testDatabase, tableInfo)
    val generatedId get() = IdType(Random.nextBytes(16))

    @Test fun getComplexOrmQuery() {
        val id = generatedId
        ComplexOrmInitializer(testDatabase, databaseSchema, tableInfo).recreateAllTables()
        ComplexOrmWriter(testDatabase, tableInfo).saveOneColumn(Model.ReaderTable::class, Model.ReaderTable::testValue, id, "test_value")
        val result = complexOrmReader.complexOrmQuery.getOneColumn(Model.ReaderTable::class, Model.ReaderTable::testValue, id, String::class)
        assertEquals("test_value", result)
    }

    @Test fun queryForEach() {
    }

    @Test fun queryMap() {
    }

    @Test fun read() {
    }
}