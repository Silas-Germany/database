package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.CommonContentValues
import com.github.silasgermany.complexorm.CommonCursor
import com.github.silasgermany.complexorm.commonUse
import com.github.silasgermany.complexorm.getCursor

interface ComplexOrmDatabaseInterface {
    fun beginTransaction()
    fun setTransactionSuccessful()
    fun endTransaction()
    fun insertWithOnConflict(table: String, nullColumnHack: String?,
                             initialValues: CommonContentValues, conflictAlgorithm: Int): Long
    fun updateWithOnConflict(table: String, values: CommonContentValues,
                             whereClause: String, whereArgs: Array<String>?, conflictAlgorithm: Int): Int
    fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int
    fun execSQL(sql: String)
    fun rawQuery(sql: String, selectionArgs: Array<String>?): CommonCursor?

    fun queryForEach(sql: String, f: (CommonCursor) -> Unit) {
        return getCursor(rawQuery(sql, null)!!).commonUse {
                it.moveToFirst()
                (0 until it.getCount()).forEach { _ -> f(it); it.moveToNext() }
            }
    }

    @Suppress("unused")
    fun queryAny(sql: String, f: (CommonCursor) -> Boolean): Boolean {
        return getCursor(rawQuery(sql, null)!!).commonUse {
                it.moveToFirst()
                (0 until it.getCount()).any { _ -> f(it).apply { it.moveToNext() } }
            }
    }

    fun <T>queryMap(sql: String, f: (CommonCursor) -> T): List<T> =
        getCursor(rawQuery(sql, null)!!).commonUse {
                it.moveToFirst()
                (0 until it.getCount()).map { _ -> f(it).apply { it.moveToNext() } }
            }

    fun getVersion(): Int
    fun setVersion(value: Int)
}