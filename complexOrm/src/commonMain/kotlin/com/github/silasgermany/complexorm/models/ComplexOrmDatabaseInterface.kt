package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.ComplexOrmCursor
import com.github.silasgermany.complexormapi.IdType

interface ComplexOrmDatabaseInterface {
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
    fun updateOne(table: String, values: Map<String, Any?>, id: IdType?) =
        id?.let { update(table, values, "id=${id.asSql}") } == 1
    fun update(table: String, values: Map<String, Any?>, whereClause: String): Int
    fun deleteOne(table: String, id: IdType?) =
        id?.let { delete(table, "id=${id.asSql}") } == 1
    fun delete(table: String, whereClause: String): Int
    fun execSQL(sql: String)
    fun <T>queryForEach(sql: String, f: (ComplexOrmCursor) -> T)
    fun <T>queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T>
    var version: Int
    fun close()
}