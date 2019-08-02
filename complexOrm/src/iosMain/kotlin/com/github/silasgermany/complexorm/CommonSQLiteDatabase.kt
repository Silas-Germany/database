package com.github.silasgermany.complexorm

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun updateWithOnConflict(
        table: String,
        values: CommonContentValues,
        whereClause: String,
        whereArgs: Array<String>?,
        conflictAlgorithm: Int
    ): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun delete(
        table: String,
        whereClause: String,
        whereArgs: Array<String>?
    ): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun execSQL(sql: String) {
    }

    actual fun rawQuery(
        sql: String,
        selectionArgs: Array<String>?
    ): CommonCursor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

actual object CommonSQLiteDatabaseObject {
    actual val CONFLICT_IGNORE: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val CONFLICT_FAIL: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val CONFLICT_ROLLBACK: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
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
    actual fun moveToFirst(): Boolean
    actual fun getCount(): Int
    actual fun moveToNext(): Boolean
}

actual fun <T> CommonCursor.commonUse(block: (CommonCursor) -> T): T {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun valueSet(): Set<Map.Entry<String, Any?>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual class CommonFile actual constructor(parent: String, child: String) {
    actual fun getParentFile(): CommonFile {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual constructor(parent: CommonFile, child: String) : this("", "") {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun listFiles(): Array<CommonFile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun delete(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun exists(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun mkdir(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

actual fun CommonFile.commonReadText(): String {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual fun CommonFile.commonWriteText(text: String) {
}

actual class CommonJSONObject actual constructor(json: String) {
    actual fun keys(): Iterator<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun put(name: String, value: Any): CommonJSONObject {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getJSONObject(name: String): CommonJSONObject {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getString(name: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual val databaseSchema: com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
actual val tableInfo: com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

actual fun KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>.isSubClassOf(table: KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual val com.github.silasgermany.complexormapi.ComplexOrmTable.longName: String
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
actual val KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>.longName: String
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
actual val KClass<*>.shortName: String
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
actual val com.github.silasgermany.complexormapi.CommonUUID?.asByteArray: ByteArray?
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
actual val ByteArray.asCommonUUID: com.github.silasgermany.complexormapi.CommonUUID
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

actual fun getCursor(
    cursor: CommonCursor,
    withColumnsInfo: Boolean
): CommonCursor {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual class DateTime actual constructor(unixMillisLong: Long) {
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