package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabase
import com.github.silasgermany.complexormapi.IdType
import kotlin.random.Random
import kotlin.test.Test

internal class ComplexOrmReaderTest {

    val testDatabase = ComplexOrmDatabase("/tmp/database.db")
    val complexOrmReader = ComplexOrmReader(testDatabase, tableInfo)
    val generatedId get() = IdType(Random.nextBytes(16))

    @Test fun getComplexOrmQuery() {
        ComplexOrmInitializer(testDatabase, databaseSchema, tableInfo).recreateAllTables()
        complexOrmReader.complexOrmQuery.getOneColumn(Model.ReaderTable::class, Model.ReaderTable::name, generatedId, String::class)
    }

    @Test fun queryForEach() {
    }

    @Test fun queryMap() {
    }

    @Test fun read() {
    }
}