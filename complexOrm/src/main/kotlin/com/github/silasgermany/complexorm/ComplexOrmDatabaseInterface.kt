package com.github.silasgermany.complexorm

import android.content.ContentValues

interface ComplexOrmDatabaseInterface {
    fun insertWithOnConflict(table: String, nullColumnHack: String,
        initialValues: ContentValues, conflictAlgorithm: Int): Long
    fun updateWithOnConflict(table: String, values: ContentValues,
        whereClause: String, whereArgs: Array<String>?, conflictAlgorithm: Int): Int
    fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int
    fun execSQL(sql: String)
}