package com.github.silasgermany.database

import android.os.Build.VERSION_CODES.LOLLIPOP
import com.github.silasgermany.complexorm.ComplexOrmReader
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.database.models.TestDatabase
import com.github.silasgermany.database.tables.AllTables
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class, sdk = [LOLLIPOP], packageName = BuildConfig.APPLICATION_ID, manifest = "/home/arch/android/rev79/database/app/src/main/AndroidManifest.xml")
class ReaderTest {

    private val databaseReader by lazy { ComplexOrmReader(TestDatabase()) }

    @Test
    fun readEmptyTable() {
        val additionalRequestData = ReadTableInfo()
        val table = databaseReader.read<AllTables.EmptyTable>(additionalRequestData)
        System.out.println("Got: $table")
        additionalRequestData.print()
    }

    @Test
    fun readNormalTable() {
        val additionalRequestData = ReadTableInfo()
        val table = databaseReader.read<AllTables.NormalTable>(additionalRequestData)
        System.out.println("Got: $table")
        additionalRequestData.print()
    }
}