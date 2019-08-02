package com.github.silasgermany.complexorm

import android.content.ContentValues
import android.database.CrossProcessCursor
import android.database.DatabaseErrorHandler
import com.github.silasgermany.complexorm.models.ComplexOrmCursor
import com.github.silasgermany.complexormapi.CommonUUID
import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import kotlin.reflect.KClass

// Get generated classes
actual val databaseSchema: ComplexOrmDatabaseSchemaInterface
    get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmDatabaseSchemaInterface
actual val tableInfo: ComplexOrmTableInfoInterface
    get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface
actual fun KClass<out ComplexOrmTable>.isSubClassOf(table: KClass<out ComplexOrmTable>) =
    java.isAssignableFrom(table.java)

// Java class information
actual val KClass<out ComplexOrmTable>.longName: String
    get() = java.name.replace("$", ".")
actual val ComplexOrmTable.longName: String get() = javaClass.name.replace("$", ".")
actual val KClass<*>.shortName: String
    get() = java.simpleName

actual val CommonUUID?.asByteArray: ByteArray?
    get() = this?.let { _ ->
        ByteBuffer.allocate(2 * Long.SIZE_BYTES)
            .putLong(getMostSignificantBits())
            .putLong(getLeastSignificantBits())
            .array()
    }
actual val ByteArray.asCommonUUID: CommonUUID
    get() = ByteBuffer.wrap(this).run { CommonUUID(long, long) }
// Factory
actual fun getCursor(cursor: CommonCursor, withColumnsInfo: Boolean): CommonCursor {
    return (cursor as? CrossProcessCursor)?.let { ComplexOrmCursor(it, withColumnsInfo) }
        ?.takeIf { ownCursor -> ownCursor.valid } ?: cursor
}

// Classes (typealias in Java)
actual typealias CommonSQLiteDatabase = android.database.sqlite.SQLiteDatabase
actual object CommonSQLiteDatabaseObject {
    actual val CONFLICT_IGNORE: Int get() = android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
    actual val CONFLICT_FAIL: Int get() = android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL
    actual val CONFLICT_ROLLBACK: Int get() = android.database.sqlite.SQLiteDatabase.CONFLICT_ROLLBACK
}
actual typealias CommonCursorFactory = android.database.sqlite.SQLiteDatabase.CursorFactory
actual typealias CommonDatabaseErrorHandler = DatabaseErrorHandler
actual typealias CommonCursor = android.database.Cursor
actual fun <T> CommonCursor.commonUse(block: (CommonCursor) -> T): T = use(block)
actual typealias CommonContentValues = ContentValues
actual typealias CommonFile = File
actual fun File.commonReadText(): String = readText()
actual fun File.commonWriteText(text: String) = writeText(text)
actual typealias CommonJSONObject = JSONObject

// todo: Create

actual class DateTime actual constructor(unixMillisLong: Long) {
    companion object {
        fun now() = DateTime(0L)
    }
    actual val unixMillisLong: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun format(formatDate: Any): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual val yearInt: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val year: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val month1: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}


actual class DateFormat