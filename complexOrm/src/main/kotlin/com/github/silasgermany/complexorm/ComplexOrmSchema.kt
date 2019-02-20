package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass

class ComplexOrmSchema(private val database: ComplexOrmDatabaseInterface) {

    private val complexOrmDatabaseSchema = Class.forName("com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmDatabaseSchemaInterface

    private val complexOrmTableInfo = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface

    inline fun <reified T: ComplexOrmTable>createTableIfNotExists() = createTableIfNotExists(T::class)
    fun <T: ComplexOrmTable>createTableIfNotExists(table: KClass<T>) {
        database.execSQL(complexOrmDatabaseSchema.createTableCommands
            .getValue(complexOrmTableInfo.basicTableInfo.getValue("${table::class}").first))
    }

    inline fun <reified T: ComplexOrmTable>dropTableIfExists() = dropTableIfExists(T::class)
    fun <T: ComplexOrmTable>dropTableIfExists(table: KClass<T>) {
        database.execSQL(complexOrmDatabaseSchema.dropTableCommands
            .getValue(complexOrmTableInfo.basicTableInfo.getValue("${table::class}").first))
    }

    inline fun <reified T: ComplexOrmTable>replaceTable() {
        dropTableIfExists<T>()
        createTableIfNotExists<T>()
    }

    fun createAllTables() {
        complexOrmDatabaseSchema.createTableCommands.values.forEach { database.execSQL(it) }
    }
}