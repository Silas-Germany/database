package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexormapi.ComplexOrmTable

class ReadTableInfo(
    val restrictions: Map<String, String> = mapOf(),
    private val alreadyLoaded: MutableMap<String, MutableMap<Int, ComplexOrmTable>> = mutableMapOf()
) {
    private val givenTables: Set<String> = alreadyLoaded.keys.toSet()
    val loadingTables: MutableSet<String> = mutableSetOf()
    val nextRequests: MutableMap<String, MutableSet<ComplexOrmTable>> = mutableMapOf()
    val notAlreadyLoaded: MutableMap<String, MutableSet<ComplexOrmTable>> = mutableMapOf()
    var missingEntries: Collection<ComplexOrmTable>? = null
    var connectedColumn: String? = null

    private fun <T, K> MutableMap<T, MutableSet<K>>.init(key: T) = getOrPut(key) { mutableSetOf() }
    private fun <T, K, V> MutableMap<T, MutableMap<K, V>>.init(key: T) = getOrPut(key) { mutableMapOf() }

    fun addRequest(tableClassName: String, table: ComplexOrmTable?) {
        table?.let { nextRequests.init(tableClassName).add(it) }
    }
    fun addMissingTable(tableClassName: String, table: ComplexOrmTable?) {
        table?.let { notAlreadyLoaded.init(tableClassName).add(it) }
    }
    fun alreadyGiven(tableClassName: String) = tableClassName in givenTables && !isMissingRequest(tableClassName)
    fun alreadyLoading(tableClassName: String) = tableClassName in loadingTables
    fun has(tableClassName: String) = alreadyLoaded.containsKey(tableClassName)
    fun getTable(tableClassName: String, id: Int?) = alreadyLoaded[tableClassName]?.get(id)
    fun setTable(tableClassName: String, table: ComplexOrmTable, specialColumnValue: String? = null) {
        (table.map[specialColumnValue ?: "id"] as Int?)?.let { alreadyLoaded.init(tableClassName)[it] = table }
    }
    fun isMissingRequest(tableClassName: String) = missingEntries?.any { it::class.java.canonicalName == tableClassName } == true
    fun print() {
        System.out.println("Other values(restrictions, ${restrictions.size}): $restrictions")
        System.out.println("Other values(alreadyLoaded, ${alreadyLoaded.flatMap { it.value.toList() }.size}): $alreadyLoaded")
        System.out.println("Other values(givenTables, ${givenTables.size}): $givenTables")
        System.out.println("Other values(nextRequests, ${nextRequests.flatMap { it.value }.size}): $nextRequests")
        System.out.println("Other values(missingEntries, ${missingEntries?.size}): $missingEntries")
        System.out.println("Other values(connectedColumn): $connectedColumn")
    }
}