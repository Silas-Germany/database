package com.github.silasgermany.database

import com.github.silasgermany.complexorm.ComplexOrmWriter
import com.github.silasgermany.database.models.TestDatabase
import com.github.silasgermany.database.tables.AllTables
import com.github.silasgermany.database.tables.AppliedTablesInterface
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class WriterTest {

    private val databaseWriter by lazy { ComplexOrmWriter(TestDatabase()) }

    @Test
    fun emptyTableWriting() {
        databaseWriter.save(AllTables.EmptyTable())
    }

    @Test
    fun appliedTableWriting() {
        val normalTable = AppliedTablesInterface.NormalTable()
        normalTable.longValue = 1L
        normalTable.dateValue = Date()
        normalTable.byteArrayValue = ByteArray(10)
        databaseWriter.save(normalTable)
    }

    @Test
    fun connectedTablesWriting() {
        val referenceTable = AllTables.ReferenceTable()
        referenceTable.normalTable = AllTables.NormalTable(1)
        referenceTable.normalTableValue = AllTables.NormalTable()
        referenceTable.normalTableValues = listOf(AllTables.NormalTable(), AllTables.NormalTable(2))
        databaseWriter.save(referenceTable)
    }

    @Test
    fun reverseConnectedTablesWriting() {
        val normalTable = AllTables.NormalTable()
        normalTable.connectedTableValues = listOf(AllTables.ReferenceTable(1))
        normalTable.otherWritingConnectedTableValues = listOf(AllTables.ReferenceTable(2))
        normalTable.columnEqualsTableNameConnectedTableValues = listOf(AllTables.ReferenceTable(3))
        normalTable.joinTableValues = listOf(AllTables.ReferenceTable(4))
        normalTable.otherWritingJoinTableValues = listOf(AllTables.ReferenceTable(5))
        databaseWriter.save(normalTable)
    }
}