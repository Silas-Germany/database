package com.github.silasgermany.complexorm

import android.annotation.SuppressLint
import android.database.CrossProcessCursor
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class SqlDatabase(private val sqlDatabase: SQLiteDatabase) {

    val requestsWithColumnInfo = false

    @SuppressLint("Recycle")
    fun queryForEach(sql: String, f: (Cursor) -> Unit) {
        return sqlDatabase.rawQuery(sql, null).run {
            (this as? CrossProcessCursor)?.let { SqlCursor(it, requestsWithColumnInfo) }?.takeIf { it.valid } ?: this
        }.run {
            Log.e("DATABASE", "$sql: $count")
            moveToFirst()
            (0 until count).forEach { _-> f(this); moveToNext() }
            close()
        }
    }

    @SuppressLint("Recycle")
    fun <T> queryMap(sql: String, f: (Cursor) -> T): List<T> = sqlDatabase.rawQuery(sql, null).run {
        (this as? CrossProcessCursor)?.let { SqlCursor(it, requestsWithColumnInfo) }?.takeIf { it.valid } ?: this
    }.run {
        moveToFirst()
        (0 until count).map { _-> f(this).also { moveToNext() } }.also { close() }
    }

    @SuppressLint("Recycle")
    fun query(sql: String): Cursor = sqlDatabase.rawQuery(sql, null).run {
        (this as? CrossProcessCursor)?.let { SqlCursor(it, requestsWithColumnInfo) }?.takeIf { it.valid } ?: this
    }

    fun rawSql(sql: String) = sqlDatabase.execSQL(sql)

}