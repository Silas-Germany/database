package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexormapi.ComplexOrmTable

class AdditionalRequestData {
    val restrictions = mapOf<String, String>()
    private val alreadyLoaded = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()
    private val givenTables = alreadyLoaded.keys
    private val nextRequests = mutableMapOf<String, MutableSet<ComplexOrmTable>>()
    val missingEntries: List<ComplexOrmTable>? = null
    var connectedColumn: String? = null

    private fun <T, K> MutableMap<T, MutableSet<K>>.init(key: T) = getOrPut(key) { mutableSetOf() }
    private fun <T, K, V> MutableMap<T, MutableMap<K, V>>.init(key: T) = getOrPut(key) { mutableMapOf() }

    fun addRequest(tableClassName: String, table: ComplexOrmTable?) {
        table?.let { nextRequests.init(tableClassName).add(it) }
    }
    fun alreadyGiven(tableClassName: String) = tableClassName in givenTables && !isMissingRequest
    fun has(tableClassName: String) = alreadyLoaded.containsKey(tableClassName)
    fun getTable(tableClassName: String, id: Long?) = alreadyLoaded[tableClassName]?.get(id)
    fun setTable(tableClassName: String, table: ComplexOrmTable?) {
        alreadyLoaded.init(tableClassName)[table?.id!!] = table
    }
    val isMissingRequest get() = missingEntries != null
    fun print() {
        System.out.println("Other values(restrictions, ${restrictions.size}): $restrictions")
        System.out.println("Other values(alreadyLoaded, ${alreadyLoaded.flatMap { it.value.toList() }.size}): $alreadyLoaded")
        System.out.println("Other values(givenTables, ${givenTables.size}): $givenTables")
        System.out.println("Other values(nextRequests, ${nextRequests.flatMap { it.value }.size}): $nextRequests")
        System.out.println("Other values(missingEntries, ${missingEntries?.size}): $missingEntries")
        System.out.println("Other values(connectedColumn): $connectedColumn")
    }
}