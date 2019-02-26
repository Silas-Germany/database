package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass

class ComplexOrmInitializer internal constructor(private val database: ComplexOrmDatabaseInterface,
                            private val complexOrmSchema: ComplexOrmDatabaseSchemaInterface, private val complexOrmTableInfo: ComplexOrmTableInfoInterface) {

    inline fun <reified T: ComplexOrmTable>createTableIfNotExists() = createTableIfNotExists(T::class)
    fun <T: ComplexOrmTable>createTableIfNotExists(table: KClass<T>) {
        val (rootTableName, rootTableClass) = complexOrmTableInfo.basicTableInfo.getValue(table.java.canonicalName)
        database.execSQL(complexOrmSchema.createTableCommands.getValue(rootTableName))
        complexOrmTableInfo.joinColumns[rootTableClass]?.keys?.forEach {
            database.execSQL(complexOrmSchema.createTableCommands.getValue("${rootTableName}_$it"))
        }
    }

    inline fun <reified T: ComplexOrmTable>dropTableIfExists() = dropTableIfExists(T::class)
    fun <T: ComplexOrmTable>dropTableIfExists(table: KClass<T>) {
        database.execSQL(complexOrmSchema.dropTableCommands
            .getValue(complexOrmTableInfo.basicTableInfo.getValue(table.java.canonicalName).first))
    }

    inline fun <reified T: ComplexOrmTable>replaceTable() {
        dropTableIfExists<T>()
        createTableIfNotExists<T>()
    }

    fun createAllTables() {
        complexOrmSchema.createTableCommands.values.forEach { database.execSQL(it) }
    }
}