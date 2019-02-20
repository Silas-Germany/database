package com.github.silasgermany.complexorm.models

import android.content.ContentValues
import android.database.CrossProcessCursor
import android.database.Cursor
import com.github.silasgermany.complexorm.ComplexOrmQueryBuilder

interface ComplexOrmDatabaseInterface {
    fun insertWithOnConflict(table: String, nullColumnHack: String,
        initialValues: ContentValues, conflictAlgorithm: Int): Long
    fun updateWithOnConflict(table: String, values: ContentValues,
        whereClause: String, whereArgs: Array<String>?, conflictAlgorithm: Int): Int
    fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int
    fun execSQL(sql: String)
    fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor?

    val query get() = ComplexOrmQueryBuilder(this)

    fun queryForEach(sql: String, f: (Cursor) -> Unit) {
        return rawQuery(sql, null)!!
            .let { cursor ->
                (cursor as? CrossProcessCursor)?.let { ComplexOrmCursor(it) }
                    ?.takeIf { ownCursor -> ownCursor.valid } ?: cursor
            }.use {
                it.moveToFirst()
                (0 until it.count).forEach { _ -> f(it); it.moveToNext() }
            }
    }
}