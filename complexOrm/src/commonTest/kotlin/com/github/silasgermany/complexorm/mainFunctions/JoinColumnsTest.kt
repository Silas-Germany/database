package com.github.silasgermany.complexorm.mainFunctions

import com.github.silasgermany.complexorm.ComplexOrm
import com.github.silasgermany.complexorm.helper.CommonHelper
import com.github.silasgermany.complexorm.helper.ComplexOrmHelper
import com.github.silasgermany.complexorm.models.Model.AdvancedReferenceTable
import com.github.silasgermany.complexorm.models.Model.AdvancedTable
import com.github.silasgermany.complexormapi.IdType
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class JoinColumnsTest: CommonHelper(), ComplexOrmHelper {

    override val database by lazy { ComplexOrm("/tmp/database.db") }
    init { resetDatabase() }

    @Test fun manyToManyConnections() {
        val writeEntry = AdvancedTable().also {
            it.normalEntries = listOf(AdvancedReferenceTable())
        }
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        assertTrue(writeEntry.normalEntries.all { it.id != null })
        val entries = database.query.get<AdvancedTable>()
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.id, readEntry.id)
        assertEquals(writeEntry.normalEntries, readEntry.normalEntries)
    }

    @Test fun reverseManyToManyConnections() {
        val writeEntry = AdvancedTable().also {
            it.reverseJoinEntries = listOf(AdvancedReferenceTable())
        }
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        assertTrue(writeEntry.reverseJoinEntries.all { it.id != null })
        val entries = database.query.get<AdvancedTable>()
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.id, readEntry.id)
        assertEquals(writeEntry.reverseJoinEntries, readEntry.reverseJoinEntries)
    }

    @Test fun manyToOneConnections() {
        val writeEntry = AdvancedTable().also {
            it.reverseNormalEntries = listOf(AdvancedReferenceTable())
        }
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        assertTrue(writeEntry.reverseNormalEntries.all { it.id != null })
        val entries = database.query.get<AdvancedTable>()
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.id, readEntry.id)
        assertEquals(writeEntry.reverseNormalEntries, readEntry.reverseNormalEntries)
    }

    @Test fun specialManyToOneConnections() {
        val writeEntry = AdvancedTable().also {
            it.reverseSpecialEntries = listOf(AdvancedReferenceTable())
        }
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        assertTrue(writeEntry.reverseSpecialEntries.all { it.id != null })
        val entries = database.query.get<AdvancedTable>()
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.id, readEntry.id)
        assertEquals(writeEntry.reverseSpecialEntries, readEntry.reverseSpecialEntries)
    }

    @Test fun specialConnection() {
        val writeEntry = AdvancedTable().also {
            it.specialConnectedEntry = AdvancedReferenceTable().apply {
                special = IdType(Random.nextBytes(16))
            }
        }
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        assertTrue(writeEntry.specialConnectedEntry?.id != null)
        val entries = database.query.get<AdvancedTable>()
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.id, readEntry.id)
        assertEquals(writeEntry.specialConnectedEntry, readEntry.specialConnectedEntry)
    }

    @Test fun allConnections() {
        val writeEntry = AdvancedTable().also {
            it.normalEntries = listOf(AdvancedReferenceTable())
            it.reverseJoinEntries = listOf(AdvancedReferenceTable())
            it.reverseNormalEntries = listOf(AdvancedReferenceTable())
            it.reverseSpecialEntries = listOf(AdvancedReferenceTable())
            it.specialConnectedEntry = AdvancedReferenceTable().apply {
                special = IdType(Random.nextBytes(16))
            }
        }
        database.save(writeEntry)
        assertNotNull(writeEntry.id)
        assertTrue(writeEntry.specialConnectedEntry?.id != null)
        val entries = database.query.get<AdvancedTable>()
        assertEquals(1, entries.size)
        val readEntry = entries.first()
        assertEquals(writeEntry.id, readEntry.id)
        assertEquals(writeEntry.normalEntries, readEntry.normalEntries)
        assertEquals(writeEntry.reverseJoinEntries, readEntry.reverseJoinEntries)
        assertEquals(writeEntry.reverseNormalEntries, readEntry.reverseNormalEntries)
        assertEquals(writeEntry.reverseSpecialEntries, readEntry.reverseSpecialEntries)
        assertEquals(writeEntry.specialConnectedEntry, readEntry.specialConnectedEntry)
    }
}