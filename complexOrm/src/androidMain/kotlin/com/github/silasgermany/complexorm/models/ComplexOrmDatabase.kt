package com.github.silasgermany.complexorm.models

import android.annotation.SuppressLint
import android.content.ContentValues
import com.github.silasgermany.complexorm.BuildConfig
import com.github.silasgermany.complexorm.CommonDateTime
import com.github.silasgermany.complexorm.CommonFile
import com.github.silasgermany.complexorm.ComplexOrmCursor
import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.IdType
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import java.util.*
import kotlin.reflect.KClass

@Suppress("OVERRIDE_BY_INLINE")
actual class ComplexOrmDatabase actual constructor(file: CommonFile, password: ByteArray?) : ComplexOrmDatabaseInterface {

    val LOG_ENABLED = BuildConfig.BUILD_TYPE == "debug"

    var database: SQLiteDatabase

    init {
        file.parentFile?.mkdirs()
        database = SQLiteDatabase.openOrCreateDatabase(file.path, password, null)
    }

    actual override inline fun <T>doInTransaction(f: () -> T): T {
        if (database.inTransaction()) return f()
        try {
            execSQL("BEGIN TRANSACTION;")
        } catch (e: SQLiteException) {
            return f()
        }
        return try {
            f().also {
                execSQL("COMMIT TRANSACTION;")
            }
        } catch (e: Throwable) {
            /*
            val foreignKeyProblems = queryMap("PRAGMA foreign_key_check;") {
                (it.getString(0) to it.getString(2)) to it.getInt(1)
            }.groupBy({ it.first }) { it.second }
            execSQL("ROLLBACK TRANSACTION;")
            if (foreignKeyProblems.isNotEmpty()) {
                throw IllegalStateException("Foreign Key Constraint failed: $foreignKeyProblems", e)
            }
             */
            execSQL("ROLLBACK TRANSACTION;")
            throw e
        }
    }
    actual override inline fun <T>doInTransactionWithDeferredForeignKeys(f: () -> T): T =
        doInTransaction {
            execSQL("PRAGMA defer_foreign_keys=ON;")
            f()
        }

    private val Map<String, Any?>.asContentValues get() = ContentValues().apply {
        forEach { (key, value) ->
            when (value) {
                null -> putNull(key)
                is IdType -> put(key, value.bytes)
                is Boolean -> put(key, value)
                is Int -> put(key, value)
                is Long -> put(key, value)
                is Float -> put(key, value)
                is String -> put(key, value)
                is Date -> put(key, value.toString())
                is CommonDateTime -> put(key, (value.millis / 1000).toInt())
                is ByteArray -> put(key, value)
                else -> throw IllegalArgumentException("$value has unknown type: ${value::class} (value for $key)")
            }
        }
    }

    override fun insertWithoutId(table: String, values: Map<String, Any?>) {
        database.insertOrThrow(table, null, values.asContentValues)
        if (LOG_ENABLED) println("Inserted $table with $values")
    }
    override fun insert(table: String, values: Map<String, Any?>): IdType {
        val valuesWithId = if (values["id"] != null) values
        else values + ("id" to IdType(UUID.randomUUID()))
        insertWithoutId(table, valuesWithId)
        return valuesWithId["id"] as IdType
    }

    override fun update(table: String, values: Map<String, Any?>, whereClause: String): Int =
        database.update(table, values.asContentValues, whereClause, null)
            .also { if (LOG_ENABLED) println("Updated $table with $values at $whereClause") }

    override fun delete(table: String, whereClause: String): Int =
        database.delete(table, whereClause, null)
            .also { if (LOG_ENABLED) println("Deleted $table at $whereClause") }

    override fun execSQL(sql: String) {
        require(sql.endsWith(';')) { "SQL commands should end with ';' ($sql)" }
        if (LOG_ENABLED) println(sql)
        database.execSQL(sql)
    }

    override fun execSQLWithBytes(sql: String, list: List<ByteArray>) {
        require(sql.endsWith(';')) { "SQL commands should end with ';' ($sql)" }
        if (LOG_ENABLED) println(sql)
        database.execSQL(sql, list.toTypedArray())
    }

    actual inline fun <reified T: Any> queryOne(sql: String): T? =
        queryOne(sql, T::class)
    @Suppress("UNCHECKED_CAST")
    actual override fun <T : Any> ComplexOrmDatabaseInterface.queryOne(sql: String, returnClass: KClass<T>): T? {
        require(sql.endsWith(';')) { "SQL commands should end with ';' ($sql)" }
        if (LOG_ENABLED) println(sql)
        val cursor = database.rawQuery(sql, null)
        ComplexOrmCursor(cursor).use {
            when (it.count) {
                1 -> {}
                0 -> return null
                else -> throw IllegalArgumentException("Request ($sql) returns more than one entry: ${it.count}")
            }
            it.moveToFirst()
            return it.get(0, returnClass)
        }

    }
    actual override inline fun queryForEach(sql: String, f: (ComplexOrmCursor) -> Unit) {
        require(sql.endsWith(';')) { "SQL commands should end with ';' ($sql)" }
        if (LOG_ENABLED) println(sql)
        val cursor = database.rawQuery(sql, null)
        ComplexOrmCursor(cursor).use {
            it.moveToFirst()
            repeat(it.count) { _ -> f(it); it.moveToNext() }
        }
    }
    @SuppressLint("Recycle")
    actual override inline fun <T> queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T> {
        require(sql.endsWith(';')) { "SQL commands should end with ';' ($sql)" }
        if (LOG_ENABLED) println(sql)
        val cursor = database.rawQuery(sql, null)
        ComplexOrmCursor(cursor).use {
            it.moveToFirst()
            return (0 until it.count).map { _ -> f(it).apply { it.moveToNext() } }
        }
    }

    override fun close() = database.close()
}
