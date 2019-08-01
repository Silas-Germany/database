package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.CommonUUID
import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass

// Classes (typealias in Java)
expect class CommonSQLiteDatabase private constructor(
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
    fun getVersion(): Int
    fun setVersion(version: Int)

    fun beginTransaction()
    fun setTransactionSuccessful()
    fun endTransaction()
    fun insertWithOnConflict(
        table: String, nullColumnHack: String?,
        initialValues: CommonContentValues, conflictAlgorithm: Int
    ): Long
    fun updateWithOnConflict(
        table: String, values: CommonContentValues,
        whereClause: String, whereArgs: Array<String>?, conflictAlgorithm: Int
    ): Int

    fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int
    fun execSQL(sql: String)
    fun rawQuery(sql: String, selectionArgs: Array<String>?): CommonCursor
}
expect object CommonSQLiteDatabaseObject {
    val CONFLICT_IGNORE: Int
    val CONFLICT_FAIL: Int
    val CONFLICT_ROLLBACK: Int
}
expect interface CommonCursorFactory
expect interface CommonDatabaseErrorHandler
expect interface CommonCursor {
    fun isNull(columnIndex: Int): Boolean
    fun getInt(columnIndex: Int): Int
    fun getLong(columnIndex: Int): Long
    fun getFloat(columnIndex: Int): Float
    fun getString(columnIndex: Int): String
    fun getBlob(columnIndex: Int): ByteArray
    fun moveToFirst(): Boolean
    fun getCount(): Int
    fun moveToNext(): Boolean
}
expect fun <T> CommonCursor.commonUse(block: (CommonCursor) -> T): T
expect class CommonContentValues() {
    fun putNull(key: String)
    fun put(key: String, value: String)
    fun put(key: String, value: Int)
    fun put(key: String, value: Boolean)
    fun put(key: String, value: Long)
    fun put(key: String, value: Float)
    fun put(key: String, value: ByteArray?)
    fun containsKey(key: String): Boolean
    fun valueSet(): Set<Map.Entry<String, Any?>>
}
expect class CommonFile(parent: String, child: String) {
    fun getParentFile(): CommonFile

    constructor(parent: CommonFile, child: String)

    fun listFiles(): Array<CommonFile>
    fun delete(): Boolean
    fun exists(): Boolean
    fun mkdir(): Boolean
}
expect fun CommonFile.commonReadText(): String
expect fun CommonFile.commonWriteText(text: String)
expect class CommonJSONObject(json: String) {
    fun keys(): Iterator<String>
    fun put(name: String, value: Any): CommonJSONObject
    fun getJSONObject(name: String): CommonJSONObject
    fun getString(name: String): String
}

// Get generated classes
expect val databaseSchema: ComplexOrmDatabaseSchemaInterface
expect val tableInfo: ComplexOrmTableInfoInterface
// Java class information
expect fun KClass<out ComplexOrmTable>.isSubClassOf(table: KClass<out ComplexOrmTable>): Boolean
expect val KClass<out ComplexOrmTable>.longName: String
expect val ComplexOrmTable.longName: String
expect val KClass<*>.shortName: String
// CommonUUID transformation
expect val CommonUUID?.asByteArray: ByteArray?
expect val ByteArray.asCommonUUID: CommonUUID
// Factory
expect fun getCursor(cursor: CommonCursor, withColumnsInfo: Boolean = true): CommonCursor

expect open class KLiveData<T> {
    var value: T?
}
expect open class KMutableLiveData<T>(): KLiveData<T>
expect class DateFormat
expect class DateTime(unixMillisLong: Long) {
    fun format(formatDate: Any): String

    val year: Int
    val month1: Int
    val yearInt: Int
    val unixMillisLong: Long
}