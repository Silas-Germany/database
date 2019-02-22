package com.github.silasgermany.complexorm

import android.database.sqlite.SQLiteDatabase
import com.github.silasgermany.complexorm.models.ComplexOrmDatabase
import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexormapi.ComplexOrmTable
import kotlin.reflect.KClass

class ComplexOrm(private val database: ComplexOrmDatabaseInterface) {
    constructor(database: SQLiteDatabase) : this(ComplexOrmDatabase(database))

    val complexOrmReader = ComplexOrmReader(database)
    val complexOrmInitializer = ComplexOrmInitializer(database)
    val complexOrmWriter = ComplexOrmWriter(database)

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

    val query get() = ComplexOrmQueryBuilder(database)
}