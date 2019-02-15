package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

interface ComplexOrmSchemaInterface {
    val tables: Map<KClass<out @JvmWildcard ComplexOrmTable>, String>
    val dropTableCommands: List<String>
    val createTableCommands: List<String>
}