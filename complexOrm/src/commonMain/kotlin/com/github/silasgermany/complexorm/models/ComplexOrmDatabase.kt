package com.github.silasgermany.complexorm.models

@Suppress("OVERRIDE_BY_INLINE")
expect class ComplexOrmDatabase(path: String) : ComplexOrmDatabaseInterface {
    override inline fun <T>doInTransaction(f: () -> T): T
}