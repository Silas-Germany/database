package com.github.silasgermany.complexorm.models

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class ComplexOrmDatabase(private val database: SQLiteDatabase): ComplexOrmDatabaseInterface {
    override fun insertWithOnConflict(table: String, nullColumnHack: String, initialValues: ContentValues, conflictAlgorithm: Int): Long {
        return database.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm)
    }

    override fun updateWithOnConflict(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>?, conflictAlgorithm: Int): Int {
        return database.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm)
    }

    override fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int {
        return database.delete(table, whereClause, whereArgs)
    }

    override fun execSQL(sql: String) {
        return database.execSQL(sql)
    }

    override fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor? {
        return database.rawQuery(sql, selectionArgs)
    }

}