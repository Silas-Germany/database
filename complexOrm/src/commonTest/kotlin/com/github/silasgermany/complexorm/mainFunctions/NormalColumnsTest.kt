package com.github.silasgermany.complexorm.mainFunctions

import com.github.silasgermany.complexorm.CommonDateTime
import com.github.silasgermany.complexorm.ComplexOrm
import com.github.silasgermany.complexorm.Model.ColumnTypesTable
import com.github.silasgermany.complexorm.Model.SimpleTable
import com.github.silasgermany.complexorm.helper.CommonHelper
import com.github.silasgermany.complexorm.helper.ComplexOrmHelper
import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.IdType
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class NormalColumnsTest: CommonHelper(), ComplexOrmHelper {

    override val database by lazy { ComplexOrm("/tmp/database.db") }
    init { resetDatabase() }

    @Test fun readWriteOneColumn() {
        val id = IdType(Random.nextBytes(16))
        database.saveOneColumn(SimpleTable::testValue, id, "test_value")
        val result = database.getOneColumn(SimpleTable::testValue, id)
        assertEquals("test_value", result)
    }

    @Test fun readWriteEntry() {
        val writeEntry = SimpleTable()
        writeEntry.testValue = "test_value"
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        val entries = database.query.get<SimpleTable>()
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

}