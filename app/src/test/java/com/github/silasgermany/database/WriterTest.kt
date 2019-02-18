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
        databaseWriter.write(AllTables.EmptyTable())
    }

    @Test
    fun appliedTableWriting() {
        val normalTable = AppliedTablesInterface.NormalTable()
        normalTable.longValue = 1L
        normalTable.dateValue = Date()
        normalTable.byteArrayValue = ByteArray(10)
        databaseWriter.write(normalTable)
    }

    @Test
    fun connectedTablesWriting() {
        val referenceTable = AllTables.ReferenceTable()
        referenceTable.normalTable = AllTables.NormalTable(1L)
        referenceTable.normalTableValue = AllTables.NormalTable()
        referenceTable.normalTableValues = listOf(AllTables.NormalTable(), AllTables.NormalTable(2L))
        databaseWriter.write(referenceTable)
    }

    @Test
    fun reverseConnectedTablesWriting() {
        val normalTable = AllTables.NormalTable()
        normalTable.connectedTableValues = listOf(AllTables.ReferenceTable(1L))
        normalTable.otherWritingConnectedTableValues = listOf(AllTables.ReferenceTable(2L))
        normalTable.columnEqualsTableNameConnectedTableValues = listOf(AllTables.ReferenceTable(3L))
        normalTable.joinTableValues = listOf(AllTables.ReferenceTable(4L))
        normalTable.otherWritingJoinTableValues = listOf(AllTables.ReferenceTable(5L))
        databaseWriter.write(normalTable)
    }
}