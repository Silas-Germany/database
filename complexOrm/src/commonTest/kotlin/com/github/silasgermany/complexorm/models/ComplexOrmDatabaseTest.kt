package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.CommonFile
import com.github.silasgermany.complexorm.CommonHelper
import com.github.silasgermany.complexormapi.IdType
import kotlin.random.Random
import kotlin.test.*

class ComplexOrmDatabaseTest: CommonHelper() {

    val database: ComplexOrmDatabase
    init {
        CommonFile("/tmp", "database.db").delete()
        database = ComplexOrmDatabase("/tmp/database.db")
        database.execSQL("CREATE TABLE test(id PRIMARY KEY, value);")
    }

    @AfterTest fun tearDown() {
        try { database.close() } catch (e: Throwable) { println("Database closing didn't work") }
    }

    @Test fun testDoInTransaction() {
        database.execSQL("CREATE TABLE reference(id, test_id REFERENCES test(id));")
        database.execSQL("PRAGMA foreign_keys=ON;")
        assertFails { database.insert("reference", mapOf("test_id" to 1)) }
        var id = database.insert("test", mapOf())
        database.insert("reference", mapOf("test_id" to id))
        id = IdType(Random.nextBytes(16))
        assertFails {
            database.doInTransaction {
                database.execSQL("PRAGMA defer_foreign_keys=ON;")
                database.insert("reference", mapOf("test_id" to id))
            }
        }
    }

    @Test fun testInsert() {
        database.insert("test", mapOf("value" to 1))
        val value = database.queryOne("SELECT value FROM test;") { it.getInt(0) }
        assertEquals(1, value)
    }

    @Test fun testUpdate() {
        database.insert("test", mapOf("value" to 1))
        database.insert("test", mapOf("value" to 2))
        database.update("test", mapOf("value" to 3), "value=1")
        val values = database.queryMap("SELECT value FROM test;") { it.getInt(0) }
        assertEquals(setOf(2, 3), values.toSet())
        database.update("test", mapOf(), "value=1")
    }

    @Test fun testDelete() {
        database.insert("test", mapOf("value" to 1))
        database.insert("test", mapOf("value" to 2))
        database.delete("test", "value=1")
        val value = database.queryOne("SELECT value FROM test;") { it.getInt(0) }
        assertEquals(2, value)
    }

    @Test fun testExecSQL() {
        val value = database
            .queryOne("SELECT name FROM sqlite_master WHERE type='table';")
            { it.getString(0) }
        assertEquals("test", value)
    }

    @Test fun testQueryForEach() {
        database.insert("test", mapOf("value" to 1))
        val first = mutableSetOf<Unit>()
        database.queryForEach("SELECT value FROM test;") {
            assertTrue(first.add(Unit))
            assertEquals(1, it.getInt(0))
        }
    }

    @Test fun testClose() {
        database.close()
        assertFails { database.doInTransaction {  } }
    }
}