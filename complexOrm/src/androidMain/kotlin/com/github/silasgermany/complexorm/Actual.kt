package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import org.joda.time.DateTime
import java.io.File
import kotlin.reflect.KClass

// Get generated classes
actual val databaseSchema: ComplexOrmDatabaseSchemaInterface
    get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema")
        .newInstance() as ComplexOrmDatabaseSchemaInterface
actual val tableInfo: ComplexOrmTableInfoInterface
    get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .newInstance() as ComplexOrmTableInfoInterface

// Java class information
actual val KClass<out ComplexOrmTable>.longName: String
    get() = java.name.replace("$", ".")

// Classes (typealias in Java)
actual typealias CommonFile = File

actual fun File.commonReadText(): String = readText()
actual fun File.commonWriteText(text: String) = writeText(text)
actual typealias CommonDateTime = DateTime
