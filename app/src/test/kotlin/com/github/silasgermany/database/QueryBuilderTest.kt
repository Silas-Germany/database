package com.github.silasgermany.database

import com.github.silasgermany.complexorm.ComplexOrmQuery
import com.github.silasgermany.complexorm.ComplexOrmQueryBuilder
import com.github.silasgermany.complexorm.ComplexOrmReader
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.database.models.TestDatabase
import com.github.silasgermany.database.tables.AllTables
import com.github.silasgermany.database.tables.AllTables.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class QueryBuilderTest {

    private val database by lazy { ComplexOrmQueryBuilder(ComplexOrmReader(TestDatabase())) }

    @Test
    fun emptyTableQuery() {
        val additionalRequestData = ReadTableInfo()
        val table = database
            .get<EmptyTable>()
        System.out.println("Got: $table")
        additionalRequestData.print()
    }

    @Test
    fun normallyRestrictedTableQuery() {
        val table = database
            .where(NormalTable::booleanValue, true)
            .where(NormalTable::longValue, 1)
            .where(NormalTable::intValue, 1)
            .where(NormalTable::floatValue, 1)
            .where(NormalTable::stringValue, "")
            .where(NormalTable::otherTableValue, 1)
            .where(NormalTable::otherTableValue, EmptyTable(1))
            .get<NormalTable>()
        NormalTable()
        System.out.println("Got: $table")
    }

    @Test
    fun specialRestrictedTableQuery() {
        val table = database
            .specialWhere(NormalTable::booleanValue, "?? != ?", true)
            .specialWhere(NormalTable::intValue, "?? != ?", null)
            .get<NormalTable>()
        NormalTable()
        System.out.println("Got: $table")
    }
}