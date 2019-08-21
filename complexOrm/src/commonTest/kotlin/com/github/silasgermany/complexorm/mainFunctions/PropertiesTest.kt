package com.github.silasgermany.complexorm.mainFunctions

import com.github.silasgermany.complexorm.ComplexOrm
import com.github.silasgermany.complexorm.Model.PropertiesReferenceTable
import com.github.silasgermany.complexorm.Model.PropertiesTable
import com.github.silasgermany.complexorm.helper.CommonHelper
import com.github.silasgermany.complexorm.helper.ComplexOrmHelper
import kotlin.random.Random
import kotlin.test.*

internal class PropertiesTest: CommonHelper(), ComplexOrmHelper {

    override val database by lazy { ComplexOrm("/tmp/database.db") }
    init {
        resetDatabase()
        database.complexOrmWriter.database.foreignKeyConstraint = true
    }

    private val generatedEntry get() = PropertiesTable().also {
        it.uniqueEntry = "entry_${Random.nextInt()}"
        it.doubleUniqueEntry1 = "entry_${Random.nextInt()}"
        it.tripleUniqueEntry1 = "entry_${Random.nextInt()}"
        it.defaultDeleteRestriction = PropertiesReferenceTable()
    }

    @Test fun uniqueRestriction() {
        var entry = generatedEntry
        entry.uniqueEntry = "same_entry"
        database.save(entry)
        entry = generatedEntry
        entry.uniqueEntry = "same_entry"
        assertFails { database.save(entry) }
        entry = generatedEntry
        entry.uniqueEntry = "another_entry"
        database.save(entry)
    }

    @Test fun doubleUniqueRestriction() {
        var entry = generatedEntry
        entry.doubleUniqueEntry1 = "same_entry1"
        entry.doubleUniqueEntry2 = "same_entry2"
        database.save(entry)
        entry = generatedEntry
        entry.doubleUniqueEntry1 = "same_entry1"
        entry.doubleUniqueEntry2 = "same_entry2"
        assertFails { database.save(entry) }
        entry = generatedEntry
        entry.doubleUniqueEntry1 = "another_entry"
        entry.doubleUniqueEntry2 = "same_entry2"
        database.save(entry)
        entry = generatedEntry
        entry.doubleUniqueEntry1 = "same_entry1"
        entry.doubleUniqueEntry2 = "another_entry"
        database.save(entry)
    }

    @Test fun tripleUniqueRestriction() {
        var entry = generatedEntry
        entry.tripleUniqueEntry1 = "same_entry1"
        entry.tripleUniqueEntry2 = "same_entry2"
        entry.tripleUniqueEntry3 = "same_entry3"
        database.save(entry)
        entry = generatedEntry
        entry.tripleUniqueEntry1 = "same_entry1"
        entry.tripleUniqueEntry2 = "same_entry2"
        entry.tripleUniqueEntry3 = "same_entry3"
        assertFails { database.save(entry) }
        entry = generatedEntry
        entry.tripleUniqueEntry1 = "another_entry"
        entry.tripleUniqueEntry2 = "same_entry2"
        entry.tripleUniqueEntry3 = "same_entry3"
        database.save(entry)
        entry = generatedEntry
        entry.tripleUniqueEntry1 = "same_entry1"
        entry.tripleUniqueEntry2 = "another_entry"
        entry.tripleUniqueEntry3 = "same_entry3"
        database.save(entry)
        entry = generatedEntry
        entry.tripleUniqueEntry1 = "same_entry1"
        entry.tripleUniqueEntry2 = "same_entry2"
        entry.tripleUniqueEntry3 = "another_entry"
        database.save(entry)
    }

    @Test fun lengthRestriction() {
        var entry = generatedEntry
        entry.maxLengthEntry = "1234"
        assertFails { database.save(entry) }
        entry = generatedEntry
        entry.maxLengthEntry = "123"
        database.save(entry)
        entry = generatedEntry
        entry.maxLengthEntry = "1"
        database.save(entry)
    }

    @Test fun defaultSetNullRestriction() {
        val entry = generatedEntry
        entry.defaultSetNullRestriction = PropertiesReferenceTable()
        database.save(entry)
        database.delete(entry.defaultSetNullRestriction)
        val entries = database.query.get<PropertiesTable>()
        assertEquals(1, entries.size)
        assertNull(entries.first().defaultSetNullRestriction)
    }

    @Test fun defaultDeleteRestriction() {
        val entry = generatedEntry
        entry.defaultDeleteRestriction = PropertiesReferenceTable()
        database.save(entry)
        database.delete(entry.defaultDeleteRestriction)
        val entries = database.query.get<PropertiesTable>()
        assertTrue(entries.isEmpty())
    }

    @Test fun deleteRestriction() {
        val entry = generatedEntry
        entry.deleteCascadeEntry = PropertiesReferenceTable()
        database.save(entry)
        database.delete(entry.deleteCascadeEntry)
        val entries = database.query.get<PropertiesTable>()
        assertTrue(entries.isEmpty())
    }

    @Test fun crashRestriction() {
        val entry = generatedEntry
        entry.deleteRestrictEntry = PropertiesReferenceTable()
        database.save(entry)
        assertFails { database.delete(entry.deleteRestrictEntry) }
    }
}