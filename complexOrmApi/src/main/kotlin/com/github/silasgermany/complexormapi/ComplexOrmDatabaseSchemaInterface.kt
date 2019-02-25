package com.github.silasgermany.complexormapi

import java.util.*
import kotlin.reflect.KClass

interface ComplexOrmDatabaseSchemaInterface {
    val tables: Map<KClass<out ComplexOrmTable>, String>
    val dropTableCommands: SortedMap<String, String>
    val createTableCommands: SortedMap<String, String>
}