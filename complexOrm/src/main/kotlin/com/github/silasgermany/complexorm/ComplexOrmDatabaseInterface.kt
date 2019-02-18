package com.github.silasgermany.complexorm

import android.content.ContentValues
import android.database.CrossProcessCursor
import android.database.Cursor
import com.github.silasgermany.complexorm.oldVersion.ComplexOrmCursor

interface ComplexOrmDatabaseInterface {
    fun insertWithOnConflict(table: String, nullColumnHack: String,
        initialValues: ContentValues, conflictAlgorithm: Int): Long
    fun updateWithOnConflict(table: String, values: ContentValues,
        whereClause: String, whereArgs: Array<String>?, conflictAlgorithm: Int): Int
    fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int
    fun execSQL(sql: String)
    fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor?

    fun queryForEach(complexOrm: String, f: (Cursor) -> Unit) {
        return rawQuery(complexOrm, null)
            .let {
                (it as? CrossProcessCursor)?.let { ComplexOrmCursor(it) }?.takeIf { ownCursor -> ownCursor.valid } ?: it
            }.use {
                it ?: return@use
                it.moveToFirst()
                (0 until it.count).forEach { _ -> f(it); it.moveToNext() }
            }
    }
}