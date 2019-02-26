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
import kotlin.reflect.KProperty1

@Suppress("UNUSED")
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

    private val tables get() = complexOrmSchema.tables
    val allTables = tables.values.toList()
    val allTableNames = tables.keys.toList()

    val KClass<out ComplexOrmTable>.name get() = java.canonicalName

    fun getNormalColumnNames(table: KClass<out ComplexOrmTable>) =
        complexOrmTableInfo.normalColumns[table.name]?.keys?.toList() ?: emptyList()
    fun getNormalColumns(table: KClass<out ComplexOrmTable>) =
        complexOrmTableInfo.normalColumns[table.name]?.toList() ?: emptyList()
    fun getConnectedColumnNames(table: KClass<out ComplexOrmTable>) =
        complexOrmTableInfo.connectedColumns[table.name]?.keys?.map { "${it}_id" } ?: emptyList()
    fun getJoinTableNames(table: KClass<out ComplexOrmTable>): List<String> {
        val tableName = table.tableName
        return complexOrmTableInfo.joinColumns[table.name]?.keys?.map { "${tableName}_$it" } ?: emptyList()
    }

    inline fun <reified T: ComplexOrmTable, R> fullColumnName(column: KProperty1<T, R>): String =
        T::class.tableName + "." + columnName(T::class, column)
    fun <T: ComplexOrmTable, R> fullColumnName(table: KClass<T>, column: KProperty1<T, R>): String =
            table.tableName + "." + columnName(table, column)

    inline fun <reified T: ComplexOrmTable, R> columnName(column: KProperty1<T, R>): String = columnName(T::class, column)
    fun <T: ComplexOrmTable, R> columnName(table: KClass<T>, column: KProperty1<T, R>): String {
        var columnName = column.name.toSql()
        if (complexOrmTableInfo.connectedColumns[table.java.canonicalName!!]?.contains(columnName) == true) columnName += "_id"
        return columnName
    }

    val query get() = ComplexOrmQueryBuilder(complexOrmReader, complexOrmTableInfo)
}