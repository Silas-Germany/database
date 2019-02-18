package com.github.silasgermany.complexorm.oldVersion

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.CrossProcessCursor
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File

class ComplexOrmDatabase {

    private val normalDatabase: SQLiteDatabase?
    private val ComplexOrmCipherDatabase: net.sqlcipher.database.SQLiteDatabase?

    constructor(ComplexOrmDatabase: SQLiteDatabase) {
        normalDatabase = ComplexOrmDatabase
        ComplexOrmCipherDatabase = null
    }

    constructor(ComplexOrmDatabase: net.sqlcipher.database.SQLiteDatabase) {
        normalDatabase = null
        ComplexOrmCipherDatabase = ComplexOrmDatabase
    }

    constructor(databaseFile: File) : this(SQLiteDatabase.openOrCreateDatabase(databaseFile, null))
    constructor(databaseFile: File, password: String) : this(net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(databaseFile, password, null))

    val reader by lazy { ComplexOrmReader(this) }
    val writer by lazy { ComplexOrmWriter(this) }

    fun <T> use(f: ComplexOrmDatabase.() -> T): T {
        normalDatabase?.beginTransaction() ?: ComplexOrmCipherDatabase!!.beginTransaction()
        try {
            return f(this).also { normalDatabase?.setTransactionSuccessful() ?: ComplexOrmCipherDatabase!!.setTransactionSuccessful() }
        } finally {
            normalDatabase?.endTransaction() ?: ComplexOrmCipherDatabase!!.endTransaction()
        }
    }

    val requestsWithColumnInfo = false

    @SuppressLint("Recycle")
    fun queryForEach(ComplexOrm: String, f: (Cursor) -> Unit) {
        return (normalDatabase?.rawQuery(ComplexOrm, null) ?: ComplexOrmCipherDatabase!!.rawQuery(ComplexOrm, null)).run {
            (this as? CrossProcessCursor)?.let { ComplexOrmCursor(it) }?.takeIf { it.valid } ?: this
        }.run {
            Log.e("DATABASE", ComplexOrm)
            moveToFirst()
            (0 until count).forEach { _-> f(this); moveToNext() }
            close()
        }
    }

    @SuppressLint("Recycle")
    fun <T> queryMap(ComplexOrm: String, f: (Cursor) -> T): List<T> = (normalDatabase?.rawQuery(ComplexOrm, null) ?: ComplexOrmCipherDatabase!!.rawQuery(ComplexOrm, null)).run {
        (this as? CrossProcessCursor)?.let {
            ComplexOrmCursor(
                it,
                requestsWithColumnInfo
            )
        }?.takeIf { it.valid } ?: this
    }.run {
        moveToFirst()
        (0 until count).map { _-> f(this).also { moveToNext() } }.also { close() }
    }

    @SuppressLint("Recycle")
    fun query(ComplexOrm: String): Cursor = (normalDatabase?.rawQuery(ComplexOrm, null) ?: ComplexOrmCipherDatabase!!.rawQuery(ComplexOrm, null)).run {
        (this as? CrossProcessCursor)?.let {
            ComplexOrmCursor(
                it,
                requestsWithColumnInfo
            )
        }?.takeIf { it.valid } ?: this
    }

    fun rawComplexOrm(ComplexOrm: String) = normalDatabase?.execSQL(ComplexOrm) ?: ComplexOrmCipherDatabase!!.execSQL(ComplexOrm)

    fun insertOrThrow(table: String, values: ContentValues) =
            normalDatabase?.insertOrThrow(table, "_id", values) ?: ComplexOrmCipherDatabase!!.insertOrThrow(table, "_id", values)

    fun update(table: String, values: ContentValues, whereClause: String) =
            normalDatabase?.update(table, values, whereClause, null) ?: ComplexOrmCipherDatabase?.update(table, values, whereClause, null)

    fun delete(table: String, whereClause: String) =
            normalDatabase?.delete(table, whereClause, null) ?: ComplexOrmCipherDatabase!!.delete(table, whereClause, null)
}