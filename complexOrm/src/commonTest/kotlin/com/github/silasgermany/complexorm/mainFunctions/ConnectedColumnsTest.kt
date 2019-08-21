package com.github.silasgermany.complexorm.mainFunctions

import com.github.silasgermany.complexorm.ComplexOrm
import com.github.silasgermany.complexorm.helper.CommonHelper
import com.github.silasgermany.complexorm.helper.ComplexOrmHelper
import com.github.silasgermany.complexorm.models.Model.SimpleReferenceTable
import com.github.silasgermany.complexorm.models.Model.SimpleTable
import com.github.silasgermany.complexormapi.ComplexOrmTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class ConnectedColumnsTest: CommonHelper(), ComplexOrmHelper {

    override val database by lazy { ComplexOrm("/tmp/database.db") }
    init { resetDatabase() }

    @Test fun writeDeep() {
        val writeEntry = SimpleTable()
        writeEntry.connectedEntry = SimpleReferenceTable().apply {
            anotherSimpleEntry = SimpleTable().apply {
                connectedEntry = SimpleReferenceTable().apply {
                    anotherSimpleEntry = SimpleTable()
                }
            }
        }
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        val entries = database.query.get<SimpleTable>().filter { it.id == writeEntry.id }
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.id, readEntry.id)
        assertEquals(writeEntry.connectedEntry!!.id, readEntry.connectedEntry!!.id)
        assertEquals(writeEntry.connectedEntry!!.anotherSimpleEntry.id, readEntry.connectedEntry!!.anotherSimpleEntry.id)
        assertEquals(writeEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry!!.id, readEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry!!.id)
        assertEquals(writeEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry!!.anotherSimpleEntry.id, readEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry!!.anotherSimpleEntry.id)
        assertNull(readEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry)
    }

    @Test fun readRecursive() {
        lateinit var innerEntry: ComplexOrmTable
        val writeEntry = SimpleTable()
        database.doInTransaction {
            writeEntry.connectedEntry = SimpleReferenceTable().apply {
                anotherSimpleEntry = SimpleTable().apply {
                    connectedEntry = SimpleReferenceTable().apply {
                        anotherSimpleEntry = SimpleTable()
                        innerEntry = anotherSimpleEntry
                    }
                }
            }
            database.save(writeEntry)
        }
        database.saveOneColumn(SimpleTable::connectedEntry, innerEntry, writeEntry.connectedEntry?.id)
        val entries = database.query.get<SimpleTable>().filter { it.id == writeEntry.id }
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.connectedEntry!!.id, readEntry.connectedEntry!!.id)
        assertEquals(writeEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry!!.anotherSimpleEntry.id, readEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry!!.anotherSimpleEntry.id)
        assertEquals(writeEntry.connectedEntry!!.id,
            readEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry!!.anotherSimpleEntry.connectedEntry!!.id)
    }
}