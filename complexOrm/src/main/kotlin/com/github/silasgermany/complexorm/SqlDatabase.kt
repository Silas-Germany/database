package com.github.silasgermany.complexorm

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.CrossProcessCursor
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File

class SqlDatabase(private val sqlDatabase: SQLiteDatabase) {

    constructor(databaseFile: File) : this(SQLiteDatabase.openOrCreateDatabase(databaseFile, null))

    val reader by lazy { SqlReader(this) }
    val writer by lazy { SqlWriter(this) }

    fun <T> use(f: SqlDatabase.() -> T): T {
        sqlDatabase.beginTransaction()
        try {
            return f(this).also { sqlDatabase.setTransactionSuccessful() }
        } finally {
            sqlDatabase.endTransaction()
        }
    }

    val requestsWithColumnInfo = false

    @SuppressLint("Recycle")
    fun queryForEach(sql: String, f: (Cursor) -> Unit) {
        return sqlDatabase.rawQuery(sql, null).run {
            (this as? CrossProcessCursor)?.let { SqlCursor(it) }?.takeIf { it.valid } ?: this
        }.run {
            Log.e("DATABASE", sql)
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

    fun insertOrThrow(table: String, values: ContentValues) = sqlDatabase.insertOrThrow(table, "_id", values)

    fun update(table: String, values: ContentValues, whereClause: String) = sqlDatabase.update(table, values, whereClause, null)

    fun delete(table: String, whereClause: String) = sqlDatabase.delete(table, whereClause, null)
}