package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.IdType
import kotlin.reflect.KClass

// Classes (typealias in Java)
actual class CommonSQLiteDatabase private actual constructor(
    path: String,
    openFlags: Int,
    cursorFactory: CommonCursorFactory,
    errorHandler: CommonDatabaseErrorHandler,
    lookasideSlotSize: Int,
    lookasideSlotCount: Int,
    idleConnectionTimeoutMs: Long,
    journalMode: String,
    syncMode: String
) {
    actual fun getVersion(): Int {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun setVersion(version: Int) {
    }

    actual fun beginTransaction() {
    }

    actual fun setTransactionSuccessful() {
    }

    actual fun endTransaction() {
    }

    actual fun insertWithOnConflict(
        table: String,
        nullColumnHack: String?,
        initialValues: CommonContentValues,
        conflictAlgorithm: Int
    ): Long {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun updateWithOnConflict(
        table: String,
        values: CommonContentValues,
        whereClause: String,
        whereArgs: Array<String>?,
        conflictAlgorithm: Int
    ): Int {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun delete(
        table: String,
        whereClause: String,
        whereArgs: Array<String>?
    ): Int {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun execSQL(sql: String) {
    }

    actual fun rawQuery(
        sql: String,
        selectionArgs: Array<String>?
    ): CommonCursor {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

actual object CommonSQLiteDatabaseObject {
    actual val CONFLICT_IGNORE: Int
        get() = null!!//TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val CONFLICT_FAIL: Int
        get() = null!!//TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val CONFLICT_ROLLBACK: Int
        get() = null!!//TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

actual interface CommonCursorFactory
actual interface CommonDatabaseErrorHandler
actual interface CommonCursor {
    actual fun isNull(columnIndex: Int): Boolean
    actual fun getInt(columnIndex: Int): Int
    actual fun getLong(columnIndex: Int): Long
    actual fun getFloat(columnIndex: Int): Float
    actual fun getString(columnIndex: Int): String
    actual fun getBlob(columnIndex: Int): ByteArray
}

actual class CommonContentValues actual constructor() {
    actual fun putNull(key: String) {
    }

    actual fun put(key: String, value: String) {
    }

    actual fun put(key: String, value: Int) {
    }

    actual fun put(key: String, value: Boolean) {
    }

    actual fun put(key: String, value: Long) {
    }

    actual fun put(key: String, value: Float) {
    }

    actual fun put(key: String, value: ByteArray?) {
    }

    actual fun containsKey(key: String): Boolean {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun valueSet(): Set<Map.Entry<String, Any?>> {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual class CommonFile actual constructor(parent: String, child: String) {
    actual fun getParentFile(): CommonFile {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual constructor(parent: CommonFile, child: String) : this("", "") {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun listFiles(): Array<CommonFile> {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun delete(): Boolean {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun exists(): Boolean {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun mkdir(): Boolean {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

actual fun CommonFile.commonReadText(): String {
    null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual fun CommonFile.commonWriteText(text: String) {
}

actual class CommonJSONObject actual constructor(json: String) {
    actual fun keys(): Iterator<String> {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun put(name: String, value: Any): CommonJSONObject {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getJSONObject(name: String): CommonJSONObject {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getString(name: String): String {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual val databaseSchema: com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
    get() = null!!//TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
actual val tableInfo: com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
    get() = null!!//TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

actual fun KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>.isSubClassOf(table: KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>): Boolean {
    null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual val KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>.longName: String
    get() = null!!//TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

actual fun getCursor(
    cursor: CommonCursor,
    withColumnsInfo: Boolean
): CommonCursor {
    null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS")
actual class CommonDateTime actual constructor(var1: Long) {
    actual open fun toString(var1: String): String {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual open fun getYear(): Int {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getMonthOfYear(): Int {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getDayOfMonth(): Int {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun plusMonths(var1: Int): CommonDateTime {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getMillis(): Long {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

actual fun generateNewId(): IdType? = null