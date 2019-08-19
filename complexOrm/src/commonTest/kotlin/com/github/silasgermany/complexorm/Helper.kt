package com.github.silasgermany.complexorm

open class Helper {

    protected val database by lazy { ComplexOrm("/tmp/database.db") }

    protected fun resetDatabase() {
        database.recreateAllTables()
    }
}