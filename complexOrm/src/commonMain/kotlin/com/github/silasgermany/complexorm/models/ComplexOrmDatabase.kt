package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.CommonFile
import com.github.silasgermany.complexorm.ComplexOrmCursor
import kotlin.reflect.KClass

@Suppress("OVERRIDE_BY_INLINE")
expect class ComplexOrmDatabase(file: CommonFile) : ComplexOrmDatabaseInterface {
    override inline fun <T> doInTransaction(f: () -> T): T
    override inline fun <T> doInTransactionWithDeferredForeignKeys(f: () -> T): T
    inline fun <reified T: Any> queryOne(sql: String): T?
    override fun <T : Any> ComplexOrmDatabaseInterface.queryOne(sql: String, returnClass: KClass<T>): T?
    override inline fun queryForEach(sql: String, f: (ComplexOrmCursor) -> Unit)
    override inline fun <T> queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T>
}