package com.github.silasgermany.complexorm

import android.database.Cursor
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import kotlin.reflect.KClass

class ComplexOrmQuery(private val database: ComplexOrmDatabaseInterface, table: KClass<out ComplexOrmTable>) {

    private var readIndex = 0

    private val complexOrmTableInfo = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface

    private val tableClassName = table.qualifiedName!!
    private val tableName = complexOrmTableInfo.basicTableInfo.getValue(tableClassName).first

    private val constructors = complexOrmTableInfo.tableConstructors
    private val normalColumns = complexOrmTableInfo.normalColumns
    private val connectedColumns = complexOrmTableInfo.connectedColumns
    private val reverseConnectedColumns = complexOrmTableInfo.reverseConnectedColumns
    private val joinColumns = complexOrmTableInfo.joinColumns
    private val reverseJoinColumns = complexOrmTableInfo.reverseJoinColumns

    private val restrictions = mutableMapOf<String, String>()
    private val notAlreadyLoaded = mutableMapOf<String, MutableList<ComplexOrmTable>>()
    private var alreadyLoaded = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()
    private var alreadyLoadedStart = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()
    private val nextRequests = mutableMapOf<String, MutableList<ComplexOrmTable>>()

    private fun <T, K, V> MutableMap<T, MutableMap<K, V>>.init(key: T) = getOrPut(key) { mutableMapOf() }
    private fun <T, K> MutableMap<T, MutableList<K>>.init(key: T) = getOrPut(key) { mutableListOf() }

    private val Map.Entry<String, String?>.last get() = value ?: key
    private val String?.reverseUnderScore get() = this!!

    fun query(
        connectedColumn: String? = null,
        where: String? = null, missingEntries: List<ComplexOrmTable>? = null
    ): List<Pair<Long?, ComplexOrmTable>> {
        val columns = mutableListOf("$tableName.id")
        columns.addColumns(tableName, missingEntries != null)
        connectedColumn?.let { columns.add(it) }
        val joins = mutableListOf<String>()
        joins.addJoins(tableName, missingEntries != null)
        where?.let { joins.add(it) } ?: joins.add("WHERE 1")
        restrictions[tableClassName]?.let { joins += "AND $it" }
        val query = "SELECT ${columns.joinToString()} FROM $tableName ${joins.joinToString(" ")};"

        val result = mutableListOf<Pair<Long?, ComplexOrmTable>>()
        database.queryForEach(query) {
            readIndex = 0
            val (connectedId, databaseEntry) = it.readColumns(tableName, missingEntries, connectedColumn)
            if (databaseEntry != null) {
                alreadyLoaded.init(tableClassName)[databaseEntry.id!!] = databaseEntry
                result.add(connectedId to databaseEntry)
            }
        }
        alreadyLoadedStart[tableClassName] = alreadyLoaded.init(tableName)
        return result
    }

    private fun databaseMapInit(id: Long?) = mutableMapOf<String, Any?>("id" to id).run {
        withDefault {
            throw IllegalAccessException("Key does not exist: $it in $this")
        }
    }

    private fun Cursor.getValue(index: Int, type: ComplexOrmTypes): Any? =
        if (isNull(index)) null
        else when (type) {
            ComplexOrmTypes.Boolean -> getLong(index) != 0L
            ComplexOrmTypes.Int -> getInt(index)
            ComplexOrmTypes.Long -> getLong(index)
            //"float" -> getFloat(index)
            ComplexOrmTypes.String -> getString(index)
            ComplexOrmTypes.ByteArray -> getBlob(index)
            //"org.threeten.bp.LocalDate" -> parse(getString(index), DateTimeFormatter.ISO_DATE)
            else -> throw IllegalArgumentException("Couldn't find type $type (${getType(index)})")
        }

    private fun MutableList<String>.addColumns(isMissing: Boolean, columnName: String? = null) {
        if (alreadyLoadedStart[tableName] != null && !isMissing) return
        normalColumns?.get(tableName)?.forEach { add("${columnName ?: tableName}.${it.key}") }
        connectedColumns?.get(tableName)?.forEach { joinTable ->
            if (reverseConnectedColumns?.get(joinTable.last)?.any { it.value.first == tableName } == true &&
                alreadyLoadedStart[joinTable.last] == null) return@forEach
            if (alreadyLoadedStart[joinTable.last] != null && !isMissing) {
                add("${columnName ?: tableName}.${joinTable.key}_id")
                return@forEach
            }
            add("${joinTable.key}.id")
            addColumns(joinTable.last, isMissing, joinTable.key)
        }
    }

    private fun Cursor.readColumns(
        tableName: String,
        missingEntries: List<ComplexOrmTable>?,
        connectedColumn: String? = null
    ): Pair<Long?, ComplexOrmTable?> {
        val id = getValue(readIndex++, ComplexOrmTypes.Long) as Long?

        val databaseMap = databaseMapInit(id)
        val alreadyLoadedTable = alreadyLoadedStart[tableName]
        if (alreadyLoadedTable != null && missingEntries == null) {
            val connectedId = connectedColumn?.let { getValue(readIndex++, ComplexOrmTypes.Long) as Long }
            alreadyLoaded[tableName]?.get(id)?.let { return connectedId to it }
            return connectedId to (alreadyLoadedTable[id] ?: constructors?.get(tableName)!!.invoke(databaseMap)
                .also { notAlreadyLoaded.init(tableName).add(it) })
        }

        normalColumns?.get(tableName)?.forEach {
            val value = getValue(readIndex++, it.value)
            databaseMap[it.key.reverseUnderScore] = value
        }
        connectedColumns?.get(tableName)?.forEach { joinTableNames ->
            if (reverseConnectedColumns?.get(joinTableNames.last)?.any { it.value.first == tableName } == true &&
                alreadyLoadedStart[joinTableNames.last] == null) return@forEach
            val (_, databaseEntry) = readColumns(joinTableNames.last, null)
            if (databaseEntry != null) {
                alreadyLoaded.init(joinTableNames.last)[databaseEntry.id!!] = databaseEntry
                val identifier = joinTableNames.key.reverseUnderScore
                if (databaseMap[identifier] == null) databaseMap[identifier] = databaseEntry
                else return null to databaseMap[identifier] as ComplexOrmTable
            } else databaseMap[joinTableNames.key.reverseUnderScore] = null
        }
        val connectedId = connectedColumn?.let { getValue(readIndex++, ComplexOrmTypes.Long) as Long }

        if (missingEntries == null) alreadyLoaded[tableName]?.get(id)?.let { return connectedId to it }

        //if (i < columnCount) databaseMap["join_id"] = getValue(i)
        if (id == null) return connectedId to null
        val databaseEntry = if (missingEntries == null) constructors?.get(tableName)!!.invoke(databaseMap)
        else missingEntries.find { it.id == id }!!.apply { map.putAll(databaseMap) }
        reverseConnectedColumns?.get(tableName)?.forEach { _ ->
            nextRequests.init(tableName).add(databaseEntry)
        }
        joinColumns?.get(tableName)?.asIterable()?.forEach { _ ->
            nextRequests.init(tableName).add(databaseEntry)
        }
        reverseJoinColumns?.get(tableName)?.forEach { _ ->
            nextRequests.init(tableName).add(databaseEntry)
        }
        return connectedId to databaseEntry
    }

    private fun MutableList<String>.addJoins(tableName: String, isMissing: Boolean, columnName: String? = null) {
        if (alreadyLoadedStart[tableName] != null && !isMissing) return
        connectedColumns?.get(tableName)?.forEach { joinTable ->
            if (alreadyLoadedStart[joinTable.last] != null && !isMissing) return@forEach
            var where = if (joinTable.last == "translation") {
                "LEFT JOIN translation AS ${joinTable.key} " +
                        "ON ${joinTable.key.removeSuffix("_translation")}_id = ${joinTable.key}.translation_code_id " +
                        "AND ${(restrictions["translation"]!!).replace("translation.", "${joinTable.key}.")}"
                //"AND ${(restrictions["translation"] ?: "translation.language_id = 1").replace("translation.", "${joinTable.key}.")}"
            } else {
                "LEFT JOIN ${joinTable.last} AS ${joinTable.key} " +
                        "ON ${joinTable.key}.id = ${columnName ?: tableName}.${joinTable.key}_id"
            }
            restrictions[joinTable.last]?.let {
                where = where.removePrefix("LEFT ") + " AND ${it.replace("${joinTable.last}.", "${joinTable.key}.")}"
            }
            add(where)
            addJoins(joinTable.last, isMissing, joinTable.key)
        }
    }
}