package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface

class ComplexOrmSchema(val execSql: (String) -> Unit) {

    val complexOrmDatabaseSchema = Class.forName("com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmDatabaseSchemaInterface

    val complexOrmTableInfo = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface

    inline fun <reified T: ComplexOrmTable>createTableIfNotExists() {
        execSql(complexOrmDatabaseSchema.createTableCommands.getValue(complexOrmTableInfo.basicTableInfo.getValue("${T::class}").first))
    }

    inline fun <reified T: ComplexOrmTable>dropTableIfExists() {
        execSql(complexOrmDatabaseSchema.dropTableCommands.getValue(complexOrmTableInfo.basicTableInfo.getValue("${T::class}").first))
    }

    inline fun <reified T: ComplexOrmTable>replaceTable() {
        dropTableIfExists<T>()
        createTableIfNotExists<T>()
    }
}