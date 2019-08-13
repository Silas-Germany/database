package com.github.silasgermany.complexorm

import android.database.Cursor
import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.IdType
import org.joda.time.DateTime
import org.json.JSONObject
import java.io.File
import kotlin.reflect.KClass

// Get generated classes
actual val databaseSchema: ComplexOrmDatabaseSchemaInterface
    get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema")
        .newInstance() as ComplexOrmDatabaseSchemaInterface
actual val tableInfo: ComplexOrmTableInfoInterface
    get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .newInstance() as ComplexOrmTableInfoInterface
actual fun KClass<out ComplexOrmTable>.isSubClassOf(table: KClass<out ComplexOrmTable>) =
    java.isAssignableFrom(table.java)

// Java class information
actual val KClass<out ComplexOrmTable>.longName: String
    get() = java.name.replace("$", ".")

// Classes (typealias in Java)
actual abstract class CommonCursor : Cursor {
    actual fun getId(columnIndex: Int): IdType = IdType(getBlob(columnIndex))
}
actual typealias CommonFile = File
actual fun File.commonReadText(): String = readText()
actual fun File.commonWriteText(text: String) = writeText(text)
actual typealias CommonJSONObject = JSONObject
actual typealias CommonDateTime = DateTime
