package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import org.json.JSONObject
import java.io.File

class ReadTableInfo constructor(
        val restrictions: Map<String, String>,
        private val alreadyLoaded: MutableMap<String, MutableMap<Int, ComplexOrmTable>>,
        private val complexOrmTableInfo: ComplexOrmTableInfoInterface
) {
    var readIndex = 0
    private val givenTables: Set<String> = alreadyLoaded.keys.toSet()
    val loadingTables: MutableSet<String> = mutableSetOf()
    val nextRequests: MutableMap<String, MutableSet<ComplexOrmTable>> = mutableMapOf()
    val notAlreadyLoaded: MutableMap<String, MutableSet<ComplexOrmTable>> = mutableMapOf()
    var missingEntries: Collection<ComplexOrmTable>? = null
    var connectedColumn: String? = null

    val cachedComplexOrmTableInfo = mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>()

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
    fun isMissingRequest(tableClassName: String) = missingEntries?.any { it.javaClass.canonicalName == tableClassName } == true
    fun print() {
        System.out.println("Other values(restrictions, ${restrictions.size}): $restrictions")
        System.out.println("Other values(alreadyLoaded, ${alreadyLoaded.flatMap { it.value.toList() }.size}): $alreadyLoaded")
        System.out.println("Other values(givenTables, ${givenTables.size}): $givenTables")
        System.out.println("Other values(nextRequests, ${nextRequests.flatMap { it.value }.size}): $nextRequests")
        System.out.println("Other values(missingEntries, ${missingEntries?.size}): $missingEntries")
        System.out.println("Other values(connectedColumn): $connectedColumn")
    }

    private fun <T, V> MutableMap<T, V>.init(key: T, value: V) = getOrPut(key) { value }
    private fun <T, K, V> MutableMap<T, MutableMap<K, V>>.init(key: T, value: Map<K, V>) = getOrPut(key) { value.toMutableMap() }

    fun getBasicTableInfoFirstValue(key: String) =
            cachedComplexOrmTableInfo.init("basicTableInfo").init("first").init(key,
                    complexOrmTableInfo.basicTableInfo.getValue(key).first)
    fun getBasicTableInfoSecondValue(key: String) =
            cachedComplexOrmTableInfo.init("basicTableInfo").init("second").init(key,
                    complexOrmTableInfo.basicTableInfo.getValue(key).second)
    fun getColumnNamesValue(key: String) =
            cachedComplexOrmTableInfo.init("columnNames").init(key,
                    complexOrmTableInfo.columnNames.getValue(key))
    fun getNormalColumnsValue(key: String) =
            cachedComplexOrmTableInfo.init("normalColumns").init(key,
                    complexOrmTableInfo.normalColumns[key] ?: mapOf())
    fun getConnectedColumnsValue(key: String) =
            cachedComplexOrmTableInfo.init("connectedColumns").init(key,
                    complexOrmTableInfo.connectedColumns[key] ?: mapOf())
    fun getReverseConnectedColumnsValue(key: String) =
            cachedComplexOrmTableInfo.init("reverseConnectedColumns").init(key,
                    complexOrmTableInfo.reverseConnectedColumns[key] ?: mapOf())
    fun getJoinColumnsValue(key: String) =
            cachedComplexOrmTableInfo.init("joinColumns").init(key,
                    complexOrmTableInfo.joinColumns[key] ?: mapOf())
    fun getReverseJoinColumnsValue(key: String) =
            cachedComplexOrmTableInfo.init("reverseJoinColumns").init(key,
                    complexOrmTableInfo.reverseJoinColumns[key] ?: mapOf())
    fun getSpecialConnectedColumnsValue(key: String) =
            cachedComplexOrmTableInfo.init("specialConnectedColumns").init(key,
                    complexOrmTableInfo.specialConnectedColumns[key] ?: mapOf())

    private var cacheFile: File? = null
    fun initFromCache(cacheFile: File?) {
        this.cacheFile = cacheFile
        cacheFile?.takeIf { it.exists() } ?: return
        val json = JSONObject(cacheFile.readText())
        json.keys().forEach { json.extract(it) }
    }

    fun writeToCache() {
        cacheFile ?: return
        val json = JSONObject()
        cachedComplexOrmTableInfo.forEach allCategories@{ (category, map) ->
            if (!map.any { it.value.isNotEmpty() }) return@allCategories
            val innerJson = JSONObject()
            map.forEach { (key, value) ->
                val infoJson = JSONObject()
                if (value.isEmpty()) return@forEach
                value.forEach { (firstString, secondString) ->
                    infoJson.put(firstString, secondString)
                }
                innerJson.put(key, infoJson)
            }
            json.put(category, innerJson)
        }
        cacheFile?.writeText(json.toString())
    }

    private fun JSONObject.extract(category: String) {
        getJSONObject(category).run {
            keys().forEach { key ->
                getJSONObject(key).run {
                    keys().forEach {
                        cachedComplexOrmTableInfo.init(category).init(key)[it] = getString(it)
                    }
                }
            }
        }
    }
}