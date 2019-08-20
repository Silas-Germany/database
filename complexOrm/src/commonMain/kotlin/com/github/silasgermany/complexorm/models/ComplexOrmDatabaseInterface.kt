package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.ComplexOrmCursor
import com.github.silasgermany.complexormapi.IdType

interface ComplexOrmDatabaseInterface {
    var foreignKeyConstraint: Boolean
    get() = queryOne("PRAGMA foreign_keys;") { it.getBoolean(0) }!!
    set(value) { execSQL("PRAGMA foreign_keys=${if (value) "ON" else "OFF"};") }
    fun <T>doInTransaction(f: () -> T): T
    fun insertOrUpdate(table: String, values: Map<String, Any?>): IdType {
        if (values["id"] != null) {
            val worked = updateOne(table, values, values["id"] as IdType)
            if (!worked) return insert(table, values)
            return values["id"] as IdType
        }
        return insert(table, values)
    }
    fun insert(table: String, values: Map<String, Any?>): IdType
    fun updateOne(table: String, values: Map<String, Any?>, id: IdType?): Boolean {
        id ?: return false
        val valuesWithoutId = values.toMutableMap().apply { remove("id") }
        return update(table, valuesWithoutId, "id=${id.asSql}") == 1
    }
    fun update(table: String, values: Map<String, Any?>, whereClause: String): Int
    fun deleteOne(table: String, id: IdType?) =
        id?.let { delete(table, "id=${id.asSql}") } == 1
    fun delete(table: String, whereClause: String): Int
    fun execSQL(sql: String)
    fun <T>queryOne(sql: String, f: (ComplexOrmCursor) -> T): T?
    fun <T>queryForEach(sql: String, f: (ComplexOrmCursor) -> T)
    fun <T>queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T>
    var version: Int
        get() {
            return queryOne("PRAGMA user_version;") {
                it.getInt(0)
            }!!
        }
        set(value) {
            execSQL("PRAGMA user_version = $value;")
        }
    fun close()
}