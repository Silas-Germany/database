package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

interface ComplexOrmDatabaseSchemaInterface {
    val tables: Map<String, KClass<out ComplexOrmTable>>
    val dropTableCommands: Map<String, String>
    val createTableCommands: Map<String, String>
}