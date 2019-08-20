package com.github.silasgermany.complexorm.helper

import com.github.silasgermany.complexorm.CommonFile
import com.github.silasgermany.complexorm.ComplexOrm
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