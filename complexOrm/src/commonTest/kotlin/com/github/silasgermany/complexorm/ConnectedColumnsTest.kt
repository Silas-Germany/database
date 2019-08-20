package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.helper.CommonHelper
import com.github.silasgermany.complexorm.helper.ComplexOrmHelper
import com.github.silasgermany.complexorm.models.Model.ReaderReferenceTable
import com.github.silasgermany.complexorm.models.Model.ReaderTable
import com.github.silasgermany.complexormapi.ComplexOrmTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class ConnectedColumnsTest: CommonHelper(), ComplexOrmHelper {

    override val database by lazy { ComplexOrm("/tmp/database.db") }
    init { resetDatabase() }

    @Test fun writeDeep() {
        val writeEntry = ReaderTable()
        writeEntry.connectedEntry = ReaderReferenceTable().apply {
            anotherReaderEntry = ReaderTable().apply {
                connectedEntry = ReaderReferenceTable().apply {
                    anotherReaderEntry = ReaderTable()
                }
            }
        }
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        val entries = database.query.get<ReaderTable>().filter { it.id == writeEntry.id }
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
        lateinit var innerEntry: ComplexOrmTable
        val writeEntry = ReaderTable()
        database.doInTransaction {
            writeEntry.connectedEntry = ReaderReferenceTable().apply {
                anotherReaderEntry = ReaderTable().apply {
                    connectedEntry = ReaderReferenceTable().apply {
                        anotherReaderEntry = ReaderTable()
                        innerEntry = anotherReaderEntry
                    }
                }
            }
            database.save(writeEntry)
        }
        database.saveOneColumn(ReaderTable::connectedEntry, innerEntry, writeEntry.connectedEntry?.id)
        val entries = database.query.get<ReaderTable>().filter { it.id == writeEntry.id }
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.connectedEntry!!.id, readEntry.connectedEntry!!.id)
        assertEquals(writeEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.anotherReaderEntry.id, readEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.anotherReaderEntry.id)
        assertEquals(writeEntry.connectedEntry!!.id,
            readEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.anotherReaderEntry.connectedEntry!!.id)
    }
}