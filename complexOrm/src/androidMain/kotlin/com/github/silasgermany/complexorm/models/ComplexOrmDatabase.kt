package com.github.silasgermany.complexorm.models

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.github.silasgermany.complexorm.CommonCursor
import com.github.silasgermany.complexorm.CommonDateTime
import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.IdType
import java.util.*

@Suppress("OVERRIDE_BY_INLINE")
actual class ComplexOrmDatabase actual constructor(path: String) : ComplexOrmDatabaseInterface {

    val database: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(path, null)

    actual override inline fun <T>doInTransaction(f: () -> T): T {
        database.beginTransaction()
        return try {
            f().also {
                database.setTransactionSuccessful()
            }
        } finally {
            database.endTransaction()
        }
    }

    private val Map<String, Any?>.asContentValues get() = ContentValues().apply {
        forEach { (key, value) ->
            if (value is IdType) put(key, value.bytes)
            else when (value) {
                is String -> put(key, value)
                is Int -> put(key, value)
                is Boolean -> put(key, value)
                is Long -> put(key, value)
                is Date -> put(key, value.asSql)
                is CommonDateTime -> put(key, (value.millis / 1000).toInt())
                is ByteArray -> put(key, value)
                is IdType -> put(key, value.bytes)
                else -> throw IllegalArgumentException()
            }
        }
    }

    override fun insert(table: String, values: Map<String, Any?>): IdType {
        val valuesWithId = if (values["id"] != null) values
        else values + ("id" to IdType(UUID.randomUUID()))
        database.insertWithOnConflict(table, null, valuesWithId.asContentValues, SQLiteDatabase.CONFLICT_ROLLBACK)
        return valuesWithId["id"] as IdType
    }

    override fun update(table: String, values: Map<String, Any?>, whereClause: String): Int =
        database.update(table, values.asContentValues, whereClause, null)

    override fun delete(table: String, whereClause: String): Int =
        database.delete(table, whereClause, null)

    override fun execSQL(sql: String) {
        database.execSQL(sql)
    }

    actual override inline fun <T> queryForEach(sql: String, f: (CommonCursor) -> T) {
        database.rawQuery(sql, null).use {
            it.moveToFirst()
            while (it.moveToNext()) f(it as CommonCursor)
        }
    }
    @SuppressLint("Recycle")
    actual override inline fun <T> queryMap(sql: String, f: (CommonCursor) -> T): List<T> {
        val cursor = database.rawQuery(sql, null)
        ComplexOrmCursor(cursor).use {
            cursor.moveToFirst()
            return (0 until cursor.count).map { _ -> f(cursor as CommonCursor).apply { cursor.moveToNext() } }
        }
    }

    override var version: Int
        get() = database.version
        set(value) { database.version = value }

    override fun close() =
        database.close()
}