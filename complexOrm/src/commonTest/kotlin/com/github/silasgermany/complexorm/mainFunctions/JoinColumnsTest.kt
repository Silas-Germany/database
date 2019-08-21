package com.github.silasgermany.complexorm.mainFunctions

import com.github.silasgermany.complexorm.ComplexOrm
import com.github.silasgermany.complexorm.helper.CommonHelper
import com.github.silasgermany.complexorm.helper.ComplexOrmHelper
import com.github.silasgermany.complexorm.models.Model.AdvancedReferenceTable
import com.github.silasgermany.complexorm.models.Model.AdvancedTable
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
}