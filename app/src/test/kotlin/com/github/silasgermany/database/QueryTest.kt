package com.github.silasgermany.database

import com.github.silasgermany.complexorm.ComplexOrmQuery
import com.github.silasgermany.complexorm.models.AdditionalRequestData
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
        val additionalRequestData = AdditionalRequestData()
        val table = databaseQuery.query(AllTables.EmptyTable::class.java.canonicalName!!, additionalRequestData)
        System.out.println("Got: $table")
        additionalRequestData.print()
    }

    @Test
    fun normalTableQuery() {
        val additionalRequestData = AdditionalRequestData()
        val table = databaseQuery.query(AllTables.NormalTable::class.java.canonicalName!!, additionalRequestData)
        System.out.println("Got: $table")
        additionalRequestData.print()
    }

    @Test
    fun specialNormalTableQuery() {
        val additionalRequestData = AdditionalRequestData()
        additionalRequestData.connectedColumn = "other_table_value_id"
        val table = databaseQuery.query(AllTables.NormalTable::class.java.canonicalName!!, additionalRequestData, "WHERE 'normal_table'.'boolean_value'=1")
        System.out.println("Got: $table")
        additionalRequestData.print()
    }

    @Test
    fun referenceTableQuery() {
        val additionalRequestData = AdditionalRequestData()
        val table = databaseQuery.query(AllTables.ReferenceTable::class.java.canonicalName!!, additionalRequestData)
        System.out.println("Got: $table")
        additionalRequestData.print()
    }
}