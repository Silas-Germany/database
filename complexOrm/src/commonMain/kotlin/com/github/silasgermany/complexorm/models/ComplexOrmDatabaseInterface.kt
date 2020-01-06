package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.ComplexOrmCursor
import com.github.silasgermany.complexormapi.IdType
import kotlin.reflect.KClass

interface ComplexOrmDatabaseInterface {
    var foreignKeyConstraint: Boolean
        get() = queryOne("PRAGMA foreign_keys;", Boolean::class)!!
        set(value) { execSQL("PRAGMA foreign_keys=${if (value) "ON" else "OFF"};") }
    fun <T>doInTransaction(f: () -> T): T
    fun <T>doInTransactionWithDeferredForeignKeys(f: () -> T): T
    fun insertOrUpdate(table: String, values: Map<String, Any?>, needsId: Boolean = true): IdType? {
        if (values["id"] != null) {
            val worked = updateOne(table, values, values["id"] as IdType)
            if (!worked) {
                insertWithoutId(table, values)
            }
            return values["id"] as IdType
        }
        return if (needsId) insert(table, values)
        else {
            insertWithoutId(table, values)
            null
        }
    }
    fun insertWithoutId(table: String, values: Map<String, Any?>)
    fun insert(table: String, values: Map<String, Any?>): IdType

    fun updateOne(table: String, values: Map<String, Any?>, id: IdType?): Boolean {
        id ?: return false
        val valuesWithoutId = values.toMutableMap().apply { remove("id") }
        if (valuesWithoutId.isEmpty()) {
            return queryOne("SELECT 1 FROM $table WHERE id=${id.asSql} LIMIT 1;", Int::class) != null
        }
        return update(table, valuesWithoutId, "id=${id.asSql}") == 1
    }
    fun update(table: String, values: Map<String, Any?>, whereClause: String): Int
    fun deleteOne(table: String, id: IdType?) =
        id?.let { delete(table, "id=${id.asSql}") } == 1
    fun delete(table: String, whereClause: String): Int
    fun execSQL(sql: String)
    fun execSQLWithBytes(sql: String, list: List<ByteArray>)
    fun <T: Any>ComplexOrmDatabaseInterface.queryOne(sql: String, returnClass: KClass<T>): T?
    fun queryForEach(sql: String, f: (ComplexOrmCursor) -> Unit)
    fun <T>queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T>
    var version: Int
        get() {
            return queryOne("PRAGMA user_version;", Int::class)!!
        }
        set(value) {
            execSQL("PRAGMA user_version = $value;")
        }
    fun close()
}