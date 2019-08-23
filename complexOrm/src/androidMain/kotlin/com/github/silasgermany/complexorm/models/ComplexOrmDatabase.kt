package com.github.silasgermany.complexorm.models

import android.annotation.SuppressLint
import android.content.ContentValues
import com.github.silasgermany.complexorm.CommonDateTime
import com.github.silasgermany.complexorm.CommonFile
import com.github.silasgermany.complexorm.ComplexOrmCursor
import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.IdType
import net.sqlcipher.database.SQLiteDatabase
import java.util.*
import kotlin.reflect.KClass

@Suppress("OVERRIDE_BY_INLINE")
actual class ComplexOrmDatabase actual constructor(file: CommonFile, password: ByteArray?) : ComplexOrmDatabaseInterface {

    var database: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(file.path, password, null)

    actual override inline fun <T>doInTransaction(f: () -> T): T {
        if (database.inTransaction()) return f()
        database.beginTransaction()
        return try {
            f().also {
                database.setTransactionSuccessful()
            }
        } finally {
            try {
                database.endTransaction()
            } catch (e: Throwable) {
                execSQL("ROLLBACK TRANSACTION;")
                throw e
            }
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
        database.insertWithOnConflict(table, null, values.asContentValues, SQLiteDatabase.CONFLICT_ROLLBACK)
    }
    override fun insert(table: String, values: Map<String, Any?>): IdType {
        val valuesWithId = if (values["id"] != null) values
        else values + ("id" to IdType(UUID.randomUUID()))
        insertWithoutId(table, valuesWithId)
        return valuesWithId["id"] as IdType
    }

    override fun update(table: String, values: Map<String, Any?>, whereClause: String): Int =
        database.update(table, values.asContentValues, whereClause, null)

    override fun delete(table: String, whereClause: String): Int =
        database.delete(table, whereClause, null)

    override fun execSQL(sql: String) {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        database.execSQL(sql)
    }

    actual inline fun <reified T: Any> queryOne(sql: String): T? =
        queryOne(sql, T::class)
    @Suppress("UNCHECKED_CAST")
    actual override fun <T : Any> ComplexOrmDatabaseInterface.queryOne(sql: String, returnClass: KClass<T>): T? {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
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
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        val cursor = database.rawQuery(sql, null)
        ComplexOrmCursor(cursor).use {
            it.moveToFirst()
            repeat(it.count) { _ -> f(it) }
        }
    }
    @SuppressLint("Recycle")
    actual override inline fun <T> queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T> {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        val cursor = database.rawQuery(sql, null)
        ComplexOrmCursor(cursor).use {
            it.moveToFirst()
            return (0 until it.count).map { _ -> f(it).apply { it.moveToNext() } }
        }
    }

    override fun close() = database.close()
}