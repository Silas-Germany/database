package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

interface ComplexOrmSchemaInterface {
    val tables: Map<KClass<out ComplexOrmTable>, String>
    val dropTableCommands: Map<String, String>
    val createTableCommands: Map<String, String>
}