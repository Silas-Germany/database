package com.github.silasgermany.database

import android.content.ContentValues
import com.github.silasgermany.complexorm.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexorm.ComplexOrmWriter
import com.github.silasgermany.database.tables.AllTables
import com.github.silasgermany.database.tables.AppliedTablesInterface
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class WriterTest {

    val databaseWriter by lazy { ComplexOrmWriter(object : ComplexOrmDatabaseInterface{
        override fun insertWithOnConflict(
            table: String,
            nullColumnHack: String,
            initialValues: ContentValues,
            conflictAlgorithm: Int
        ): Long {
            System.out.println("Insert $initialValues in $table (null -> $nullColumnHack, conflictAlgorithm: $conflictAlgorithm)")
            return 0L
        }

        override fun updateWithOnConflict(
            table: String,
            values: ContentValues,
            whereClause: String,
            whereArgs: Array<String>?,
            conflictAlgorithm: Int
        ): Int {
            System.out.println("Update $values in $table where $whereClause (whereArguments -> $whereArgs, conflictAlgorithm: $conflictAlgorithm)")
            return 1
        }

        override fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int {
            System.out.println("Delete from $table where $whereClause (whereArguments -> $whereArgs)")
            return 1
        }

        override fun execSQL(sql: String) {
            System.out.println("Exec SQL: $sql")
        }
    }) }

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