package com.github.silasgermany.complexorm.models

import cnames.structs.sqlite3
import cnames.structs.sqlite3_stmt
import com.github.silasgermany.complexorm.CommonDateTime
import com.github.silasgermany.complexorm.ComplexOrmCursor
import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.IdType
import kotlinx.cinterop.*
import sqlite3.*
import uuid.uuid_generate
import uuid.uuid_t
import kotlin.reflect.KClass

@Suppress("OVERRIDE_BY_INLINE")
actual class ComplexOrmDatabase actual constructor(path: String) : ComplexOrmDatabaseInterface {

    fun Int.checkResult() {
        if (this != SQLITE_OK) {
            val message = sqlite3_errmsg(db)?.toKString()
            throw IllegalStateException("Error ($this): $message")
        }
    }

    val db = memScoped {
        val databasePointer = allocPointerTo<sqlite3>()
        sqlite3_open(path, databasePointer.ptr).checkResult()
        databasePointer.value
    }

    actual override inline fun <T>doInTransaction(f: () -> T): T {
        if (sqlite3_get_autocommit(db) == 0) return f()
         execSQL("BEGIN TRANSACTION;")
        return try {
            f().also {
                execSQL("COMMIT TRANSACTION;")
            }
        } catch (e: Throwable) {
            execSQL("ROLLBACK TRANSACTION;")
            throw e
        }
    }
    actual override inline fun <T>doInTransactionWithDeferredForeignKeys(f: () -> T): T =
        doInTransaction {
            execSQL("PRAGMA defer_foreign_keys=ON;")
            f()
        }

    private fun execSqlWithBlob(sql: String, blobValues: MutableList<ByteArray>) {
        println("${blobValues.size} -> $sql")
        useSqlStatement(sql) {
            (0 until blobValues.size).forEach { index ->
                sqlite3_bind_blob(
                    it,
                    index + 1,
                    blobValues[index].refTo(0),
                    blobValues[index].size,
                    SQLITE_TRANSIENT
                ).checkResult()
            }
            if (sqlite3_step(it) != SQLITE_DONE) {
                throw IllegalStateException(sqlite3_errmsg(db)?.toKString())
            }
        }
        blobValues.clear()
    }

    private fun Any?.sqlValue(blobValues: MutableList<ByteArray>) =
        when (this) {
            null -> "null"
            is IdType -> asSql
            is Boolean -> if (this) "1" else "0"
            is String -> "'$this'"
            is Date -> asSql
            is CommonDateTime -> "${this.getMillis() / 1000}"
            is ByteArray -> {
                blobValues.add(this)
                "?"
            }
            else -> "$this"
        }

    override fun insert(table: String, values: Map<String, Any?>): IdType {
        // For UUID Id only
        val valuesWithId = if (values["id"] != null) values
        else {
            val id = memScoped {
                val id: uuid_t = allocArray(16)
                uuid_generate(id)
                IdType(id.readBytes(16))
            }
            values + ("id" to id)
        }
        val blobValues = mutableListOf<ByteArray>()
        val sql = "INSERT OR REPLACE INTO $table(${valuesWithId.keys.joinToString(",")})" +
                "VALUES(${valuesWithId.values.joinToString(",") { it.sqlValue(blobValues) }});"
        if (blobValues.isEmpty()) execSQL(sql)
        else execSqlWithBlob(sql, blobValues)
        return valuesWithId["id"] as IdType
    }

    override fun update(table: String, values: Map<String, Any?>, whereClause: String): Int {
        val blobValues = mutableListOf<ByteArray>()
        val sql = "UPDATE $table SET " +
            "${values.entries.joinToString(",") { "${it.key}=${it.value.sqlValue(blobValues)}" }} " +
            "WHERE $whereClause;"
        if (blobValues.isEmpty()) execSQL(sql)
        else execSqlWithBlob(sql, blobValues)
        return sqlite3_changes(db)
    }

    override fun delete(table: String, whereClause: String): Int {
        val sql = "DELETE FROM $table WHERE $whereClause;"
        execSQL(sql)
        return sqlite3_changes(db)
    }
    
    override fun execSQL(sql: String) {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        sqlite3_exec(db, sql, null, null, null).checkResult()
    }

    class Cursor(private val it: CPointer<sqlite3_stmt>) : ComplexOrmCursor {
        override fun isNull(columnIndex: Int): Boolean =
            sqlite3_column_type(it, columnIndex) == SQLITE_NULL
        override fun getInt(columnIndex: Int): Int =
            sqlite3_column_int(it, columnIndex)
        override fun getLong(columnIndex: Int): Long =
            sqlite3_column_double(it, columnIndex).toLong()
        override fun getFloat(columnIndex: Int): Float =
            sqlite3_column_double(it, columnIndex).toFloat()
        override fun getString(columnIndex: Int): String {
            val size = sqlite3_column_bytes(it, columnIndex)
            return sqlite3_column_text(it, columnIndex)?.readBytes(size)?.stringFromUtf8() ?: ""
        }
        override fun getBlob(columnIndex: Int): ByteArray {
            val size = sqlite3_column_bytes(it, columnIndex)
            return sqlite3_column_blob(it, columnIndex)?.readBytes(size) ?: byteArrayOf()
        }
    }

    inline fun <T>useSqlStatement(sqlQuery: String, useStatement: (CPointer<sqlite3_stmt>) -> T): T {
        val sqlStatement = memScoped {
            val itPointer = allocPointerTo<sqlite3_stmt>()
            sqlite3_prepare_v2(db, sqlQuery,
                -1, itPointer.ptr, null).checkResult()
            itPointer.value
        } ?: throw IllegalArgumentException("Couldn't create it")
        try {
            return useStatement(sqlStatement)
        } finally {
            sqlite3_finalize(sqlStatement).checkResult()
        }
    }

    actual inline fun <reified T: Any> queryOne(sql: String): T? =
        queryOne(sql, T::class)
    @Suppress("UNCHECKED_CAST")
    actual override fun <T : Any> ComplexOrmDatabaseInterface.queryOne(sql: String, returnClass: KClass<T>): T? {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        useSqlStatement(sql) {
            when (sqlite3_step(it)) {
                SQLITE_ROW -> {}
                SQLITE_DONE -> return null
                else -> throw IllegalStateException(sqlite3_errmsg(db)?.toKString())
            }
            return Cursor(it).get(0, returnClass).apply {
                if (sqlite3_step(it) != SQLITE_DONE)
                    throw IllegalArgumentException("Request ($sql) returns more than one entry")
            }
        }
    }
    actual override inline fun queryForEach(sql: String, f: (ComplexOrmCursor) -> Unit) {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        useSqlStatement(sql) {
            var stepStatus: Int = sqlite3_step(it)
            while (stepStatus == SQLITE_ROW) {
                f(Cursor(it))
                stepStatus = sqlite3_step(it)
            }
            if (stepStatus != SQLITE_DONE) {
                throw IllegalStateException(sqlite3_errmsg(db)?.toKString())
            }
        }
    }
    actual override inline fun <T> queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T> {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        useSqlStatement(sql) {
            var stepStatus: Int = sqlite3_step(it)
            val result = mutableListOf<T>()
            while (stepStatus == SQLITE_ROW) {
                result.add(f(Cursor(it)))
                stepStatus = sqlite3_step(it)
            }
            if (stepStatus != SQLITE_DONE) {
                throw IllegalStateException(sqlite3_errmsg(db)?.toKString())
            }
            return result
        }
    }

    override fun close() {
        sqlite3_close_v2(db).checkResult()
    }
}