package com.github.silasgermany.database

import android.database.sqlite.SQLiteDatabase
import android.os.Build.VERSION_CODES.LOLLIPOP
import com.github.silasgermany.complexorm.ComplexOrmReader
import com.github.silasgermany.complexorm.ComplexOreInitializer
import com.github.silasgermany.complexorm.ComplexOrmWriter
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.database.models.RealDatabase
import com.github.silasgermany.database.tables.AllTables
import com.github.silasgermany.database.tables.AppliedTablesInterface
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class, sdk = [LOLLIPOP], packageName = BuildConfig.APPLICATION_ID, manifest = "/home/arch/android/rev79/database/app/src/main/AndroidManifest.xml")
class RealDatabaseTest {

    private val databaseWriter by lazy { ComplexOrmWriter(RealDatabase(db)) }
    private val databaseReader by lazy { ComplexOrmReader(RealDatabase(db)) }
    private val databaseInitizer by lazy { ComplexOreInitializer(RealDatabase(db)) }

    private val dbFile = File("/tmp/test")
    private val db: SQLiteDatabase by lazy {
        dbFile.delete()
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).also {
            ComplexOreInitializer(RealDatabase(it)).createAllTables()
        }
    }

    @Test
    fun createTable() {
        databaseInitizer.replaceTable<AllTables.NormalTable>()
    }

    @Test
    fun readEmptyTable() {
        val additionalRequestData = ReadTableInfo()
        val table = databaseReader.read<AllTables.EmptyTable>(additionalRequestData)
        System.out.println("Got: $table")
        additionalRequestData.print()
    }

    @Test
    fun readNormalTable() {
        val normalTable = AppliedTablesInterface.NormalTable()
        normalTable.booleanValue = true
        normalTable.floatValue = 1F
        normalTable.longValue = 1L
        normalTable.dateValue = Date(Date().time - 1000 * 60 * 60 * 24)
        normalTable.byteArrayValue = ByteArray(10) { 'X'.toByte() }
        databaseWriter.write(normalTable)
        databaseWriter.write(normalTable.clone(false).apply { stringValue = "325" })

        val additionalRequestData = ReadTableInfo()
        val tables = databaseReader.read<AllTables.NormalTable>(additionalRequestData)
        System.out.println("Got: $tables")
        assertEquals(2, tables.size)

        var table = tables.first()
        additionalRequestData.print()
        assertEquals(normalTable.booleanValue, table.booleanValue)
        assertEquals(normalTable.floatValue, table.floatValue)
        assertEquals(normalTable.longValue, table.longValue)
        assertEquals(normalTable.dateValue, table.dateValue)
        assertEquals(normalTable.byteArrayValue.toList(), table.byteArrayValue?.toList())
        assertEquals("123", table.stringValue)

        // Check default values:
        assertEquals(1, table.intValue)
        assertEquals(1L, table.longValue)
        assertEquals(1F, table.floatValue)
        assertNull(table.otherTableValue)
        assertEquals(emptyList(), table.otherTableValues)
        assertEquals(emptyList(), table.connectedTableValues)
        assertEquals(emptyList(), table.otherWritingConnectedTableValues)
        assertEquals(emptyList(), table.columnEqualsTableNameConnectedTableValues)
        assertEquals(emptyList(), table.joinTableValues)
        assertEquals(emptyList(), table.otherWritingJoinTableValues)

        table = tables[1]
        additionalRequestData.print()
        assertEquals(!normalTable.booleanValue, table.booleanValue)
        assertEquals(normalTable.floatValue, table.floatValue)
        assertEquals(normalTable.longValue, table.longValue)
        assertEquals(normalTable.dateValue, table.dateValue)
        assertEquals(normalTable.byteArrayValue.toList(), table.byteArrayValue?.toList())
        assertEquals("325", table.stringValue)
        // Check default values:
        assertEquals(1, table.intValue)
        assertEquals(1L, table.longValue)
        assertEquals(1F, table.floatValue)
        assertNull(table.otherTableValue)
        assertEquals(emptyList(), table.otherTableValues)
        assertEquals(emptyList(), table.connectedTableValues)
        assertEquals(emptyList(), table.otherWritingConnectedTableValues)
        assertEquals(emptyList(), table.columnEqualsTableNameConnectedTableValues)
        assertEquals(emptyList(), table.joinTableValues)
        assertEquals(emptyList(), table.otherWritingJoinTableValues)
    }
}