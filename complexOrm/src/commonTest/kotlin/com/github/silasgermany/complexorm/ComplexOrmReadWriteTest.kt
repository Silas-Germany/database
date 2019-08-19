package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.Model.*
import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.IdType
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
        assertEquals(writeEntry.id, readEntry.id)
    }

    @Test fun readWriteAllColumnTypes() {
        val writeEntry = ColumnTypesTable()
        writeEntry.boolean = true
        writeEntry.int = 100
        writeEntry.long = Int.MAX_VALUE + 10L
        writeEntry.float = 0.5F
        writeEntry.string = "test_value"
        writeEntry.date = Date(2019, 4, 30)
        writeEntry.dateTime = CommonDateTime(1549202706000L)
        writeEntry.byteArray = Random.nextBytes(2000)
        writeEntry.nullableEntry = "test_value"
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        val entries = database.query.get<ColumnTypesTable>()
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.id, readEntry.id)
        assertEquals(writeEntry.boolean, readEntry.boolean)
        assertEquals(writeEntry.int, readEntry.int)
        assertEquals(writeEntry.long, readEntry.long)
        assertEquals(writeEntry.float, readEntry.float)
        assertEquals(writeEntry.date.asSql, readEntry.date.asSql)
        assertEquals(writeEntry.dateTime.getMillis(), readEntry.dateTime.getMillis())
        assertEquals(writeEntry.byteArray.toList(), readEntry.byteArray.toList())
        assertEquals(writeEntry.nullableEntry, readEntry.nullableEntry)
    }

    @Test fun writeDeep() {
        val id = generatedId
        val writeEntry = ReaderTable(mutableMapOf("id" to id))
        writeEntry.connectedEntry = ReaderReferenceTable().apply {
            anotherReaderEntry = ReaderTable().apply {
                connectedEntry = ReaderReferenceTable().apply {
                    anotherReaderEntry = ReaderTable()
                }
            }
        }
        database.save(writeEntry)
        val entries = database.query.get<ReaderTable>().filter { it.id == id }
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.id, readEntry.id)
        assertEquals(writeEntry.connectedEntry!!.id, readEntry.connectedEntry!!.id)
        assertEquals(writeEntry.connectedEntry!!.anotherReaderEntry.id, readEntry.connectedEntry!!.anotherReaderEntry.id)
        assertEquals(writeEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.id, readEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.id)
        assertEquals(writeEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.anotherReaderEntry.id, readEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.anotherReaderEntry.id)
        assertNull(readEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.anotherReaderEntry.connectedEntry)
    }

    @Test fun readRecursive() {
        val id = generatedId
        val innerId = generatedId
        val writeEntry = ReaderTable(mutableMapOf("id" to id))
        writeEntry.connectedEntry = ReaderReferenceTable().apply {
            anotherReaderEntry = ReaderTable().apply {
                connectedEntry = ReaderReferenceTable().apply {
                    anotherReaderEntry = ReaderTable(mutableMapOf("id" to innerId))
                            // It's not saving entries that have an ID
                        .also { database.save(it) }
                }
            }
        }
        database.save(writeEntry)
        database.saveOneColumn(ReaderTable::connectedEntry, innerId, writeEntry.connectedEntry?.id)
        val entries = database.query.get<ReaderTable>().filter { it.id == id }
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.connectedEntry!!.id, readEntry.connectedEntry!!.id)
        assertEquals(writeEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.anotherReaderEntry.id, readEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.anotherReaderEntry.id)
        assertEquals(writeEntry.connectedEntry!!.id,
            readEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.id)
    }
}