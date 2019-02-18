package com.github.silasgermany.database

import com.github.silasgermany.complexorm.ComplexOrmQuery
import com.github.silasgermany.database.models.TestDatabase
import com.github.silasgermany.database.tables.AllTables
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class QueryTest {

    private val databaseQuery by lazy { ComplexOrmQuery(TestDatabase()) }

    @Test
    fun emptyTableQuery() {
        databaseQuery.query(AllTables.EmptyTable::class)
    }
}