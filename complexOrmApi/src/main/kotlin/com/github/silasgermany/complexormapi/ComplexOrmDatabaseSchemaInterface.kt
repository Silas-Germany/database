package com.github.silasgermany.complexormapi

import java.util.*
import kotlin.reflect.KClass

interface ComplexOrmDatabaseSchemaInterface {
    val tables: SortedMap<String, KClass<out ComplexOrmTable>>
    val dropTableCommands: SortedMap<String, String>
    val createTableCommands: SortedMap<String, String>
}