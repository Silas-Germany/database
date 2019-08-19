package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.Model.ReaderTable
import com.github.silasgermany.complexormapi.IdType
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ComplexOrmReadWriteTest: Helper() {

    private val database = ComplexOrm("/tmp/database.db")
    private val generatedId get() = IdType(Random.nextBytes(16))

    init {
        database.recreateAllTables()
    }

    @Test fun readWriteOneColumn() {
        val id = generatedId
        database.saveOneColumn(ReaderTable::testValue, id, "test_value")
        val result = database.getOneColumn(ReaderTable::testValue, id)
        assertEquals("test_value", result)
    }

    @Test fun readWriteEntry() {
        val writeEntry = ReaderTable()
        writeEntry.testValue = "test_value"
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        val entries = database.query.get<ReaderTable>()
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.testValue, readEntry.testValue)
        assertNotNull(readEntry.id)
    }
}