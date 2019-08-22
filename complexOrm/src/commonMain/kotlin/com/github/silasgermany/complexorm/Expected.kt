package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass

expect fun CommonFile.commonReadText(): String
expect fun CommonFile.commonWriteText(text: String)

// Get generated classes
expect val databaseSchema: ComplexOrmDatabaseSchemaInterface
expect val tableInfo: ComplexOrmTableInfoInterface
expect val KClass<out ComplexOrmTable>.longName: String
val ComplexOrmTable.longName get() = this::class.longName