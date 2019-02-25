package com.github.silasgermany.complexorm

import android.database.sqlite.SQLiteDatabase
import com.github.silasgermany.complexorm.models.ComplexOrmDatabase
import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import java.io.File
import kotlin.reflect.KClass

class ComplexOrm(database: ComplexOrmDatabaseInterface, cacheDir: File? = null) {
    constructor(database: SQLiteDatabase, cacheDir: File? = null) : this(ComplexOrmDatabase(database), cacheDir)

    private val complexOrmSchema = Class.forName("com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema")
            .getDeclaredField("INSTANCE").get(null) as ComplexOrmDatabaseSchemaInterface

    private val complexOrmTableInfo = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
            .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface


    val complexOrmReader = ComplexOrmReader(database, cacheDir, complexOrmTableInfo)
    val complexOrmInitializer = ComplexOrmInitializer(database, complexOrmSchema, complexOrmTableInfo)
    val complexOrmWriter = ComplexOrmWriter(database, complexOrmTableInfo)

    inline fun <reified T : ComplexOrmTable> read(readTableInfo: ReadTableInfo) =
            complexOrmReader.read<T>(readTableInfo)
    fun <T : ComplexOrmTable> read(table: KClass<T>, readTableInfo: ReadTableInfo) =
            complexOrmReader.read(table, readTableInfo)

    inline fun <reified T: ComplexOrmTable>createTableIfNotExists() =
            complexOrmInitializer.createTableIfNotExists<T>()
    fun <T: ComplexOrmTable>createTableIfNotExists(table: KClass<T>) =
            complexOrmInitializer.createTableIfNotExists(table)
    inline fun <reified T: ComplexOrmTable>dropTableIfExists() =
            complexOrmInitializer.dropTableIfExists<T>()
    fun <T: ComplexOrmTable>dropTableIfExists(table: KClass<T>) =
            complexOrmInitializer.dropTableIfExists(table)
    inline fun <reified T: ComplexOrmTable>replaceTable() =
            complexOrmInitializer.replaceTable<T>()
    fun createAllTables() =
            complexOrmInitializer.createAllTables()

    fun save(table: ComplexOrmTable, writeDeep: Boolean = true) =
            complexOrmWriter.save(table, writeDeep)

    val query get() = ComplexOrmQueryBuilder(complexOrmReader, complexOrmTableInfo)
}