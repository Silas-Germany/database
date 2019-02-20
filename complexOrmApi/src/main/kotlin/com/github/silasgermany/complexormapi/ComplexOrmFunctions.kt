package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


private val complexOrmDatabaseSchema get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmDatabaseSchemaInterface
private val tables get() = complexOrmDatabaseSchema.tables
private val complexOrmTableInfo get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface
private val connectedColumns get() = complexOrmTableInfo.connectedColumns
private fun String.toSql() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()

val <T: ComplexOrmTable> KClass<T>.tableName: String get() = java.simpleName!!.toSql()
fun <T: ComplexOrmTable> KClass<T>.getTableName(table: KClass<T>): String = java.simpleName!!.toSql()

inline val <reified T: ComplexOrmTable, R> KProperty1<T, R>.fullColumnName: String get() =
    T::class.tableName + "." + getColumnName(T::class)
fun <T: ComplexOrmTable, R> KProperty1<T, R>.fullColumnName(table: KClass<T>): String =
        table.tableName + "." + getColumnName(table)

inline val <reified T: ComplexOrmTable, R> KProperty1<T, R>.columnName: String get() = getColumnName(T::class)
fun <T: ComplexOrmTable, R> KProperty1<T, R>.getColumnName(table: KClass<T>): String {
    var columnName = name.toSql()
    if (connectedColumns[table.java.canonicalName!!]?.contains(columnName) == true) columnName += "_id"
    return columnName
}

val allTables = tables.keys.toList()

val allTableNames = tables.values.toList()