package com.github.silasgermany.complexorm.models

import android.content.ContentValues
import com.github.silasgermany.complexorm.CommonCursor
import com.github.silasgermany.complexorm.CommonDateTime
import com.github.silasgermany.complexorm.CommonSQLiteDatabase
import com.github.silasgermany.complexormapi.Day
import com.github.silasgermany.complexormapi.IdType

@Suppress("OVERRIDE_BY_INLINE")
actual class ComplexOrmDatabase(val database: CommonSQLiteDatabase):
    ComplexOrmDatabaseInterface {

    override inline fun doInTransaction(f: () -> Unit) {
        database.beginTransaction()
        try {
            f()
            database.setTransactionSuccessful()
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
                is Day -> put(key, value.asSql)
                is CommonDateTime -> put(key, (value.millis / 1000).toInt())
                is ByteArray -> put(key, value)
                else -> throw IllegalArgumentException()
            }
        }
    }

    override fun insert(table: String, values: Map<String, Any?>): Long =
        database.insertWithOnConflict(table, null, values.asContentValues, 0)

    override fun update(table: String, values: Map<String, Any?>, whereClause: String): Int =
        database.update(table, values.asContentValues, whereClause, null)

    override fun delete(table: String, whereClause: String): Int =
        database.delete(table, whereClause, null)

    override fun execSQL(sql: String) {
        database.execSQL(sql)
    }

    override inline fun <T> queryForEach(sql: String, f: (CommonCursor) -> T) {
        database.rawQuery(sql, null).use {
            it.moveToFirst()
            while (it.moveToNext()) f(it)
        }
    }
    override inline fun <T> queryMap(sql: String, f: (CommonCursor) -> T): List<T> =
        database.rawQuery(sql, null).use {
            it.moveToFirst()
            (0 until it.count).map { _ -> f(it).apply { it.moveToNext() } }
        }

    override var version: Int
        get() = database.version
        set(value) { database.version = value }

    override fun close() =
        database.close()
}