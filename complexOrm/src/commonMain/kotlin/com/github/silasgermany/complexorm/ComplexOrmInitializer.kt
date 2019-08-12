package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import kotlin.reflect.KClass

class ComplexOrmInitializer internal constructor(
    private val database: ComplexOrmDatabaseInterface,
    private val complexOrmSchema: ComplexOrmDatabaseSchemaInterface
) {

    inline fun <reified T: ComplexOrmTable>createTableIfNotExists() = createTableIfNotExists(T::class)
    fun <T: ComplexOrmTable>createTableIfNotExists(table: KClass<T>) {
        complexOrmSchema.tables.filter { it.value.isSubClassOf(table) }.forEach {
            database.execSQL(complexOrmSchema.createTableCommands.getValue(it.key))
        }
    }

    inline fun <reified T: ComplexOrmTable>dropTableIfExists() = dropTableIfExists(T::class)
    fun <T: ComplexOrmTable>dropTableIfExists(table: KClass<T>) {
        complexOrmSchema.tables.filter { it.value.isSubClassOf(table) }.forEach {
            database.execSQL(complexOrmSchema.dropTableCommands.getValue(it.key))
        }
    }

    inline fun <reified T: ComplexOrmTable>replaceTable() = replaceTable(T::class)
    fun <T: ComplexOrmTable>replaceTable(table: KClass<T>) {
        dropTableIfExists(table)
        createTableIfNotExists(table)
    }

    fun createAllTables() {
        complexOrmSchema.createTableCommands.values.forEach { database.execSQL(it) }
    }


    var version: Int
        get() = database.version
        set(value) { database.version = value }
}