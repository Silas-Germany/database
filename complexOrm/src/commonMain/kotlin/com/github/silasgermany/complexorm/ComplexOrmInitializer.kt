package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabase
import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass

class ComplexOrmInitializer internal constructor(
    private val database: ComplexOrmDatabase,
    private val complexOrmSchema: ComplexOrmDatabaseSchemaInterface,
    private val tableInfo: ComplexOrmTableInfoInterface
) {

    inline fun <reified T: ComplexOrmTable>createTableIfNotExists() = createTableIfNotExists(T::class)
    fun <T: ComplexOrmTable>createTableIfNotExists(table: KClass<T>) {
        val originTable = tableInfo.basicTableInfo.getValue(table.longName).second
        complexOrmSchema.tables.filter { it.value.longName == originTable }.forEach {
            database.execSQL(complexOrmSchema.createTableCommands.getValue(it.key))
        }
    }

    inline fun <reified T: ComplexOrmTable>dropTableIfExists() = dropTableIfExists(T::class)
    fun <T: ComplexOrmTable>dropTableIfExists(table: KClass<T>) {
        val originTable = tableInfo.basicTableInfo.getValue(table.longName).second
        complexOrmSchema.tables.filter { it.value.longName == originTable }.forEach {
            database.execSQL(complexOrmSchema.dropTableCommands.getValue(it.key))
        }
    }

    inline fun <reified T: ComplexOrmTable>replaceTable() = replaceTable(T::class)
    fun <T: ComplexOrmTable>replaceTable(table: KClass<T>) {
        dropTableIfExists(table)
        createTableIfNotExists(table)
    }

    fun recreateAllTables() {
        complexOrmSchema.dropTableCommands.values.forEach { database.execSQL(it) }
        complexOrmSchema.createTableCommands.values.forEach { database.execSQL(it) }
    }

    fun createAllTables() {
        complexOrmSchema.createTableCommands.values.forEach { database.execSQL(it) }
    }


    var version: Int
        get() = database.version
        set(value) { database.version = value }
}