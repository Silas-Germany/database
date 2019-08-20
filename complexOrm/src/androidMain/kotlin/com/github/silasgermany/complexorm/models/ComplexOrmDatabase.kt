package com.github.silasgermany.complexorm.models

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.github.silasgermany.complexorm.CommonDateTime
import com.github.silasgermany.complexorm.ComplexOrmCursor
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
            when (value) {
                null -> putNull(key)
                is IdType -> put(key, value.bytes)
                is Boolean -> put(key, value)
                is Int -> put(key, value)
                is Long -> put(key, value)
                is Float -> put(key, value)
                is String -> put(key, value)
                is Date -> put(key, value.toString())
                is CommonDateTime -> put(key, (value.millis / 1000).toInt())
                is ByteArray -> put(key, value)
                else -> throw IllegalArgumentException("$value has unknown type: ${value::class} (value for $key)")
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

    actual override inline fun <T> queryForEach(sql: String, f: (ComplexOrmCursor) -> T) {
        val cursor = database.rawQuery(sql, null)
        ComplexOrmCursor(cursor).use {
            it.moveToFirst()
            repeat(it.count) { _ -> f(it) }
        }
    }
    @SuppressLint("Recycle")
    actual override inline fun <T> queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T> {
        val cursor = database.rawQuery(sql, null)
        ComplexOrmCursor(cursor).use {
            it.moveToFirst()
            return (0 until it.count).map { _ -> f(it).apply { it.moveToNext() } }
        }
    }

    override fun close() = database.close()
}