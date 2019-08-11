package com.github.silasgermany.complexorm.models

import cnames.structs.sqlite3
import cnames.structs.sqlite3_stmt
import com.github.silasgermany.complexorm.CommonCursor
import com.github.silasgermany.complexorm.CommonDateTime
import com.github.silasgermany.complexormapi.Day
import kotlinx.cinterop.*
import sqlite3.*
import kotlin.experimental.and

@Suppress("OVERRIDE_BY_INLINE")
actual class ComplexOrmDatabase(path: String) : ComplexOrmDatabaseInterface {
    
    fun Int.checkResult() {
        if (this != 0) {
            throw IllegalStateException(sqlite3_errmsg(db)?.toKString())
        }
    }

    val db = memScoped {
        val databasePointer = allocPointerTo<sqlite3>()
        sqlite3_open(path, databasePointer.ptr).checkResult()
        databasePointer.value
    }

    override inline fun doInTransaction(f: () -> Unit) {
        execSQL("BEGIN TRANSACTION;")
        try {
            f()
            execSQL("COMMIT TRANSACTION;")
        } catch (e: Throwable) {
            execSQL("ROLLBACK TRANSACTION;")
            throw e
        }
    }

    private val ByteArray.asHex: String get() {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(size * 2)
        indices.forEach {
            val lowestByte = (this[it] and 0xFF.toByte()).toInt()
            hexChars[it * 2] = hexArray[lowestByte ushr 4]
            hexChars[it * 2 + 1] = hexArray[lowestByte and 0x0F]
        }
        return String(hexChars)
    }

    private val Any?.sqlValue get() = 
        when (this) {
            null -> "null"
            is String -> "'$this'"
            is Boolean -> if (this) "1" else "0"
            is Day -> this.asSql
            is CommonDateTime -> "${this.getMillis() / 1000}"
            is ByteArray -> "x'${this.asHex}'"
            else -> "$this"
        }

    override fun insert(
        table: String,
        values: Map<String, Any?>
    ): Long {
        val sql = "INSERT OR REPLACE INTO $table(${values.keys.joinToString(",")})" +
                "VALUES(${values.values.joinToString(",") { it.sqlValue }});"
        execSQL(sql)
        return sqlite3_last_insert_rowid(db)
    }

    override fun update(table: String, values: Map<String, Any?>, whereClause: String): Int {
        val sql = "UPDATE $table SET " +
                "${values.entries.joinToString(",") { "${it.key}=${it.value.sqlValue}" }} " +
                "WHERE $whereClause;"
        execSQL(sql)
        return sqlite3_changes(db)
    }

    override fun delete(table: String, whereClause: String): Int {
        val sql = "DELETE FROM $table WHERE $whereClause;"
        execSQL(sql)
        return sqlite3_changes(db)
    }
    
    override fun execSQL(sql: String) {
        sqlite3_exec(db, sql, null, null, null).checkResult()
    }

    class Cursor(private val cursor: CPointer<sqlite3_stmt>) : CommonCursor {
        override fun isNull(columnIndex: Int): Boolean =
            sqlite3_column_type(cursor, columnIndex) == SQLITE_NULL
        override fun getInt(columnIndex: Int): Int =
            sqlite3_column_int(cursor, columnIndex)
        override fun getLong(columnIndex: Int): Long =
            sqlite3_column_double(cursor, columnIndex).toLong()
        override fun getFloat(columnIndex: Int): Float =
            sqlite3_column_double(cursor, columnIndex).toFloat()
        override fun getString(columnIndex: Int): String {
            val size = sqlite3_column_bytes(cursor, columnIndex)
            return sqlite3_column_text(cursor, columnIndex)?.readBytes(size)?.stringFromUtf8() ?: ""
        }
        override fun getBlob(columnIndex: Int): ByteArray {
            val size = sqlite3_column_bytes(cursor, columnIndex)
            return sqlite3_column_blob(cursor, columnIndex)?.readBytes(size) ?: byteArrayOf()
        }
    }

    fun createCursor(sqlQuery: String) = memScoped {
        val cursorPointer = allocPointerTo<sqlite3_stmt>()
        sqlite3_prepare_v2(db, sqlQuery,
            -1, cursorPointer.ptr, null).checkResult()
        cursorPointer.value
    } ?: throw IllegalArgumentException("Couldn't create cursor")

    override inline fun <T> queryForEach(sql: String, f: (CommonCursor) -> T) {
        val cursor = createCursor(sql)
        try {
            var stepStatus: Int = sqlite3_step(cursor)
            while (stepStatus == SQLITE_ROW) {
                f(Cursor(cursor))
                stepStatus = sqlite3_step(cursor)
            }
            if (stepStatus != SQLITE_DONE) {
                throw IllegalStateException(sqlite3_errmsg(db)?.toKString())
            }
        } finally {
            sqlite3_finalize(cursor).checkResult()
        }
    }
    override inline fun <T> queryMap(
        sql: String,
        f: (CommonCursor) -> T
    ): List<T> {
        val cursor = createCursor(sql)
        try {
            var stepStatus: Int = sqlite3_step(cursor)
            val result = mutableListOf<T>()
            while (stepStatus == SQLITE_ROW) {
                result.add(f(Cursor(cursor)))
                stepStatus = sqlite3_step(cursor)
            }
            if (stepStatus != SQLITE_DONE) {
                throw IllegalStateException(sqlite3_errmsg(db)?.toKString())
            }
            return result
        } finally {
            sqlite3_finalize(cursor).checkResult()
        }
    }

    override var version: Int
        get() {
            val cursor = createCursor("PRAGMA schema_version;")
            try {
                if (sqlite3_step(cursor) == SQLITE_ROW) {
                    return sqlite3_column_int(cursor, 0)
                } else {
                    throw IllegalStateException(sqlite3_errmsg(db)?.toKString())
                }
            } finally {
                sqlite3_finalize(cursor).checkResult()
            }
        }
        set(value) {
            execSQL("PRAGMA schema_version = $value;")
        }

    override fun close() {
        sqlite3_close(db).checkResult()
    }
}