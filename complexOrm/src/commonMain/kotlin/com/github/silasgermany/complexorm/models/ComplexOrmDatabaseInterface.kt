package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.CommonCursor
import com.github.silasgermany.complexormapi.IdType

interface ComplexOrmDatabaseInterface {
    fun doInTransaction(f: () -> Unit)
    fun insert(table: String, values: Map<String, Any?>): Long
    fun updateOne(table: String, values: Map<String, Any?>, id: IdType) =
        update(table, values, "id=$id") == 1
    fun update(table: String, values: Map<String, Any?>, whereClause: String): Int
    fun deleteOne(table: String, id: IdType) =
        delete(table, "id=$id") == 1
    fun delete(table: String, whereClause: String): Int
    fun execSQL(sql: String)
    fun <T>queryForEach(sql: String, f: (CommonCursor) -> T)
    fun <T>queryMap(sql: String, f: (CommonCursor) -> T): List<T>
    var version: Int
    fun close()
}