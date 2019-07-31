package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexorm.*
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.CommonUUID

class ReadTableInfo constructor(
    val restrictions: Map<String, String>,
    private val alreadyLoaded: MutableMap<String, MutableMap<CommonUUID, ComplexOrmTable>>,
    private val complexOrmTableInfo: ComplexOrmTableInfoInterface
) {
    var readIndex = 0
    private val givenTables: Set<String> = alreadyLoaded.keys.toSet()
    val loadingTables: MutableSet<String> = mutableSetOf()
    val nextRequests: MutableMap<String, MutableSet<ComplexOrmTable>> = mutableMapOf()
    val notAlreadyLoaded: MutableMap<String, MutableSet<ComplexOrmTable>> = mutableMapOf()
    var missingEntries: Collection<ComplexOrmTable>? = null
    var connectedColumn: String? = null

    @Suppress("MemberVisibilityCanBePrivate")
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
    fun getTable(tableClassName: String, id: CommonUUID?) = alreadyLoaded[tableClassName]?.get(id)
    fun setTable(tableClassName: String, table: ComplexOrmTable, specialColumnValue: String? = null) {
        (table.map[specialColumnValue ?: "id"] as CommonUUID?)?.let { alreadyLoaded.init(tableClassName)[it] = table }
    }
    fun isMissingRequest(tableClassName: String) = missingEntries?.any { it.longName == tableClassName } == true

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

    private var cacheFile: CommonFile? = null
    fun initFromCache(cacheFile: CommonFile?) {
        this.cacheFile = cacheFile
        cacheFile?.takeIf { it.exists() } ?: return
        val json = CommonJSONObject(cacheFile.commonReadText())
        json.keys().forEach { json.extract(it) }
    }

    fun writeToCache() {
        val cacheFile = cacheFile ?: return
        val json = CommonJSONObject(cacheFile.commonReadText())
        cachedComplexOrmTableInfo.forEach allCategories@{ (category, map) ->
            if (!map.any { it.value.isNotEmpty() }) return@allCategories
            val innerJson = CommonJSONObject(cacheFile.commonReadText())
            map.forEach { (key, value) ->
                val infoJson = CommonJSONObject(cacheFile.commonReadText())
                if (value.isEmpty()) return@forEach
                value.forEach { (firstString, secondString) ->
                    infoJson.put(firstString, secondString)
                }
                innerJson.put(key, infoJson)
            }
            json.put(category, innerJson)
        }
        cacheFile.commonWriteText(json.toString())
    }

    private fun CommonJSONObject.extract(category: String) {
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