package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.ComplexOrmCursor

@Suppress("OVERRIDE_BY_INLINE")
expect class ComplexOrmDatabase(path: String) : ComplexOrmDatabaseInterface {
    override inline fun <T>doInTransaction(f: () -> T): T
    override inline fun <T> queryForEach(sql: String, f: (ComplexOrmCursor) -> T)
    override inline fun <T> queryMap(sql: String, f: (ComplexOrmCursor) -> T): List<T>
}