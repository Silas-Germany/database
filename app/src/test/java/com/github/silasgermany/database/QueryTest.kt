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
        val table = databaseQuery.query(AllTables.EmptyTable::class.java.canonicalName!!)
        System.out.println("Got: $table")
    }

    @Test
    fun normalTableQuery() {
        val table = databaseQuery.query(AllTables.NormalTable::class.java.canonicalName!!)
        System.out.println("Got: $table")
        databaseQuery.run {
            System.out.println("Other values(restrictions, ${restrictions.size}): $restrictions")
            System.out.println("Other values(notAlreadyLoaded, ${notAlreadyLoaded.flatMap { it.value }.size}): $notAlreadyLoaded")
            System.out.println("Other values(alreadyLoaded, ${alreadyLoaded.flatMap { it.value.toList() }.size}): $alreadyLoaded")
            System.out.println("Other values(alreadyLoadedStart, ${alreadyLoadedStart.flatMap { it.value.toList() }.size}): $alreadyLoadedStart")
            System.out.println("Other values(nextRequests, ${nextRequests.flatMap { it.value }.size}): $nextRequests")
        }
    }

    @Test
    fun specialNormalTableQuery() {
        val table = databaseQuery.query(AllTables.NormalTable::class.java.canonicalName!!, "other_table_value_id")
        System.out.println("Got: $table")
        databaseQuery.run {
            System.out.println("Other values(restrictions, ${restrictions.size}): $restrictions")
            System.out.println("Other values(notAlreadyLoaded, ${notAlreadyLoaded.flatMap { it.value }.size}): $notAlreadyLoaded")
            System.out.println("Other values(alreadyLoaded, ${alreadyLoaded.flatMap { it.value.toList() }.size}): $alreadyLoaded")
            System.out.println("Other values(alreadyLoadedStart, ${alreadyLoadedStart.flatMap { it.value.toList() }.size}): $alreadyLoadedStart")
            System.out.println("Other values(nextRequests, ${nextRequests.flatMap { it.value }.size}): $nextRequests")
        }
    }

    @Test
    fun referenceTableQuery() {
        val table = databaseQuery.query(AllTables.ReferenceTable::class.java.canonicalName!!)
        System.out.println("Got: $table")
        databaseQuery.run {
            System.out.println("Other values(restrictions, ${restrictions.size}): $restrictions")
            System.out.println("Other values(notAlreadyLoaded, ${notAlreadyLoaded.flatMap { it.value }.size}): $notAlreadyLoaded")
            System.out.println("Other values(alreadyLoaded, ${alreadyLoaded.flatMap { it.value.toList() }.size}): $alreadyLoaded")
            System.out.println("Other values(alreadyLoadedStart, ${alreadyLoadedStart.flatMap { it.value.toList() }.size}): $alreadyLoadedStart")
            System.out.println("Other values(nextRequests, ${nextRequests.flatMap { it.value }.size}): $nextRequests")
        }
    }
}