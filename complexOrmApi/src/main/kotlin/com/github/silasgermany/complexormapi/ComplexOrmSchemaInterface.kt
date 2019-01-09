package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

interface ComplexOrmSchemaInterface {
    val tableNames: List<String>
    val tables: List<KClass<out @JvmWildcard ComplexOrmTable>>
    val dropTableCommands: List<String>
    val createTableCommands: List<String>
}