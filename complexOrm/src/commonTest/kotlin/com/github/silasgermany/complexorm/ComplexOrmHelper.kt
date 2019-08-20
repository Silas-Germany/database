package com.github.silasgermany.complexorm

import kotlin.test.AfterTest

interface ComplexOrmHelper {

    val database: ComplexOrm

    fun resetDatabase() {
        CommonFile("/tmp", "database.db").delete()
        database.createAllTables()
    }

    @AfterTest fun tearDown() {
        try { database.close() } catch (e: Throwable) { println("Database closing didn't work") }
    }

}