package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmTable
import kotlin.reflect.KClass

class ComplexOrmReader<T : ComplexOrmTable>(private val database: ComplexOrmDatabaseInterface, table: KClass<T>) {
/*
    private val complexOrmTableInfo = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface

    private val tableClass = table.qualifiedName!!
    private val tableName = complexOrmTableInfo.basicTableInfo.getValue(tableClass).first

    private val constructors get() = complexOrmTableInfo.tableConstructors[tableClass]
    private val normalColumns get() = complexOrmTableInfo.normalColumns[tableClass]
    private val connectedColumns get() = complexOrmTableInfo.connectedColumns[tableClass]
    private val reverseConnectedColumns get() = complexOrmTableInfo.reverseConnectedColumns[tableClass]
    private val joinColumns get() = complexOrmTableInfo.joinColumns[tableClass]
    private val reverseJoinColumns get() = complexOrmTableInfo.reverseJoinColumns[tableClass]


    private val nextRequests = mutableMapOf<String, MutableList<ComplexOrmTable>>()
    private val notAlreadyLoaded = mutableMapOf<String, MutableList<ComplexOrmTable>>()
    private var alreadyLoaded = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()
    private var alreadyLoadedStart = mutableMapOf<String, Map<Long, ComplexOrmTable>>()

    private val KProperty1<out ComplexOrmTable, Any?>.columnName get() = this.name
    private val KClass<out ComplexOrmTable>.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(qualifiedName!!).first

    private fun <T, K, V> MutableMap<T, MutableMap<K, V>>.init(key: T) = getOrPut(key) { mutableMapOf() }
    private fun <T, K> MutableMap<T, MutableList<K>>.init(key: T) = getOrPut(key) { mutableListOf() }

    private fun  read(

    ): List<T> {
        alreadyLoaded = existingEntries
        alreadyLoadedStart = existingEntries.toList().associateTo(mutableMapOf()) { it.first to it.second.toMap() }

        val result = ComplexOrmQuery().query(tableName, null).map { it.second }

        while (notAlreadyLoaded.isNotEmpty()) {
            val missingEntryTable = notAlreadyLoaded.keys.first()
            val missingEntries = notAlreadyLoaded[missingEntryTable]!!
            query(
                missingEntryTable,
                where = "WHERE $missingEntryTable._id IN (${missingEntries.joinToString { "${it.id}" }})",
                missingEntries = missingEntries
            )
            notAlreadyLoaded.remove(missingEntryTable)
        }

        while (nextRequests.isNotEmpty()) {
            val connectedTable = nextRequests.keys.first()

            val ids = nextRequests[connectedTable]!!.map { it.id!! }.toSet()
            reverseConnectedColumns?.get(connectedTable)?.forEach {
                val connectedTableName = it.value.first
                val connectedColumn = "${it.value.second ?: connectedTable}_id"
                val where = "WHERE $connectedTableName.${it.value.second
                    ?: connectedTable}_id IN (${ids.joinToString()})"

                val joinValues = query(connectedTableName, "$connectedTableName.$connectedColumn AS join_column", where)

                val transformedValues = joinValues.groupBy { it.first }
                nextRequests[connectedTable]!!.forEach { entry ->
                    val id = entry.id!!
                    entry.map[it.key.reverseUnderScore] = transformedValues[id]?.map { value ->
                        value.second.map[(it.value.second ?: connectedTable).reverseUnderScore] = entry
                        value.second
                    }.orEmpty()
                    entry.map[it.key.reverseUnderScore] = joinValues.mapNotNull { joinEntry ->
                        joinEntry.second.takeIf { joinEntry.first == id }
                                ?.apply { map[(it.value.second ?: connectedTable).reverseUnderScore] = entry }
                    }
                }
            }
            joinColumns?.get(connectedTable)?.forEach {
                val connectedColumn = "${connectedTable}_id"
                val where =
                    "LEFT JOIN ${connectedTable}_${it.key} AS join_table ON join_table.${it.value}_id = ${it.value}._id " +
                            "WHERE join_table.${connectedTable}_id IN (${ids.joinToString()})"

                val joinValues = query(it.value, "join_table.$connectedColumn", where)

                nextRequests[connectedTable]!!.forEach { entry ->
                    val id = entry.id!!
                    entry.map[it.key.reverseUnderScore] = joinValues
                        .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                }
            }
            reverseJoinColumns?.get(connectedTable)?.forEach {
                val connectedColumn = "${connectedTable}_id"
                val where =
                    "LEFT JOIN ${it.value.first}_${it.value.second} AS join_table ON join_table.${it.value.first}_id = ${it.value.first}.id " +
                            "WHERE join_table.${connectedTable}_id IN (${ids.joinToString()})"

                val joinValues = query(it.value.first, "join_table.$connectedColumn", where)

                nextRequests[connectedTable]!!.forEach { entry ->
                    val id = entry.id!!
                    entry.map[it.key.reverseUnderScore] = joinValues
                        .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                }
            }
            nextRequests.remove(connectedTable)
        }
        @Suppress("UNCHECKED_CAST")
        return (result as List<T>)
    }

*/
}