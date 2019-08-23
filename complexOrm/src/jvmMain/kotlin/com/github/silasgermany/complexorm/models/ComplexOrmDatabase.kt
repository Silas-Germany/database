package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.CommonDateTime
import com.github.silasgermany.complexorm.ComplexOrmCursor
import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.IdType
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.*
import kotlin.reflect.KClass

@Suppress("OVERRIDE_BY_INLINE", "unused")
actual class ComplexOrmDatabase actual constructor(path: String, password: ByteArray?) : ComplexOrmDatabaseInterface {

    val database: Connection = DriverManager.getConnection("jdbc:sqlite:$path")

    actual override inline fun <T>doInTransaction(f: () -> T): T {
        if (!database.autoCommit) return f()
        database.autoCommit = false
        return try {
            f().also {
                database.commit()
            }
        } catch (e: Throwable) {
            database.rollback()
            throw e
        } finally {
            database.autoCommit = true
        }
    }
    actual override inline fun <T>doInTransactionWithDeferredForeignKeys(f: () -> T): T =
        doInTransaction {
            execSQL("PRAGMA defer_foreign_keys=ON;")
            f()
        }

    private fun execSqlWithBlob(sql: String, blobValues: MutableList<ByteArray>): Int {
        println("${blobValues.size} -> $sql")
        val statement = database.prepareStatement(sql)
        (0 until blobValues.size).forEach { index ->
            statement.setBytes(index + 1, blobValues[index])
        }
        return statement.executeUpdate()
            .also { blobValues.clear() }
    }

    private fun Any?.sqlValue(blobValues: MutableList<ByteArray>) =
        when (this) {
            null -> "null"
            is IdType -> asSql
            is Boolean -> if (this) "1" else "0"
            is String -> "'$this'"
            is Date -> asSql
            is CommonDateTime -> "${this.millis / 1000}"
            is ByteArray -> {
                blobValues.add(this)
                "?"
            }
            else -> "$this"
        }

    override fun insertWithoutId(table: String, values: Map<String, Any?>) {
        val blobValues = mutableListOf<ByteArray>()
        val sql = "INSERT INTO $table(${values.keys.joinToString(",")})" +
                "VALUES(${values.values.joinToString(",") { it.sqlValue(blobValues) }});"
        if (blobValues.isEmpty()) execSQL(sql)
        else execSqlWithBlob(sql, blobValues)
    }

    override fun insert(table: String, values: Map<String, Any?>): IdType {
        // For UUID Id only
        val valuesWithId = if (values["id"] != null) values
        else {
            val id = IdType(UUID.randomUUID())
            values + ("id" to id)
        }
        insertWithoutId(table, valuesWithId)
        return valuesWithId["id"] as IdType
    }

    override fun update(table: String, values: Map<String, Any?>, whereClause: String): Int {
        val blobValues = mutableListOf<ByteArray>()
        val sql = "UPDATE $table SET " +
                "${values.entries.joinToString(",") { "${it.key}=${it.value.sqlValue(blobValues)}" }} " +
                "WHERE $whereClause;"
        return if (blobValues.isEmpty()) execSQLWithResult(sql)
        else execSqlWithBlob(sql, blobValues)
    }

    override fun delete(table: String, whereClause: String): Int {
        val sql = "DELETE FROM $table WHERE $whereClause;"
        return execSQLWithResult(sql)
    }

    override fun execSQL(sql: String) {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        val statement = database.createStatement()
        statement.execute(sql)
        statement.close()
    }

    private fun execSQLWithResult(sql: String): Int {
        println(sql)
        val statement = database.createStatement()
        return statement.executeUpdate(sql)
            .also { statement.close() }
    }

    class Cursor(private val resultSet: ResultSet) : ComplexOrmCursor {
        override fun isNull(columnIndex: Int): Boolean =
            resultSet.getBytes(columnIndex + 1) == null
        override fun getInt(columnIndex: Int): Int =
            resultSet.getInt(columnIndex + 1)
        override fun getLong(columnIndex: Int): Long =
            resultSet.getLong(columnIndex + 1)
        override fun getFloat(columnIndex: Int): Float =
            resultSet.getFloat(columnIndex + 1)
        override fun getString(columnIndex: Int): String =
            resultSet.getString(columnIndex + 1)
        override fun getBlob(columnIndex: Int): ByteArray =
            resultSet.getBytes(columnIndex + 1)
    }

    inline fun <T>useSqlStatement(sql: String, useStatement: (ResultSet) -> T): T {
        val statement = database.createStatement()
        @Suppress("ConvertTryFinallyToUseCall")
        var resultSet: ResultSet? = null
        try {
            resultSet = statement.executeQuery(sql)
            return useStatement(resultSet)
        } finally {
            resultSet?.close()
            statement.close()
        }
    }

    actual inline fun <reified T: Any> queryOne(sql: String): T? =
        queryOne(sql, T::class)
    @Suppress("UNCHECKED_CAST")
    actual override fun <T : Any> ComplexOrmDatabaseInterface.queryOne(sql: String, returnClass: KClass<T>): T? {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        useSqlStatement(sql) {
            if (!it.next()) return null
            return Cursor(it).get(0, returnClass).apply {
                if (it.next()) throw IllegalArgumentException("Request ($sql) returns more than one entry")
            }
        }
    }

    actual override inline fun queryForEach(sql: String, f: (ComplexOrmCursor) -> Unit) {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        useSqlStatement(sql) {
            while (it.next()) {
                f(Cursor(it))
            }
        }
    }

    actual override inline fun <T> queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T> {
        if (!sql.endsWith(';')) throw IllegalArgumentException("SQL commands should end with ';' ($sql)")
        println(sql)
        useSqlStatement(sql) {
            val result = mutableListOf<T>()
            while (it.next()) {
                result.add(f(Cursor(it)))
            }
            return result
        }
    }

    override fun close() =
        database.close()
}