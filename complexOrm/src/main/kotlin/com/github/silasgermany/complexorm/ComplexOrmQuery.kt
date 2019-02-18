package com.github.silasgermany.complexorm

import android.database.Cursor
import com.github.silasgermany.complexorm.models.RequestData
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import org.threeten.bp.LocalDate
import java.util.*

class ComplexOrmQuery(private val database: ComplexOrmDatabaseInterface) {

    private var readIndex = 0

    private val complexOrmTableInfo = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface

    private val constructors = complexOrmTableInfo.tableConstructors
    private val normalColumns = complexOrmTableInfo.normalColumns
    private val connectedColumns = complexOrmTableInfo.connectedColumns
    private val reverseConnectedColumns = complexOrmTableInfo.reverseConnectedColumns
    private val joinColumns = complexOrmTableInfo.joinColumns
    private val reverseJoinColumns = complexOrmTableInfo.reverseJoinColumns

    val restrictions = mapOf<String, String>()
    val notAlreadyLoaded = mutableMapOf<String, MutableList<ComplexOrmTable>>()
    var alreadyLoaded = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()
    var alreadyLoadedStart = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()
    var givenTables = setOf<String>()
    val nextRequests = mutableMapOf<String, MutableSet<ComplexOrmTable>>()

    private fun <T, K, V> MutableMap<T, MutableMap<K, V>>.init(key: T) = getOrPut(key) { mutableMapOf() }
    private fun <T, K> MutableMap<T, MutableList<K>>.init(key: T) = getOrPut(key) { mutableListOf() }
    private fun <T, K> MutableMap<T, MutableSet<K>>.init(key: T) = getOrPut(key) { mutableSetOf() }
    private val String.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(this).first

    private val String?.reverseUnderScore get() = this!!

    fun query(
        tableClassName: String, connectedColumn: String? = null,
        where: String? = null, missingEntries: List<ComplexOrmTable>? = null
    ): List<Pair<Long?, ComplexOrmTable>> {
        givenTables = alreadyLoaded.keys

        val requestData = RequestData(tableClassName.tableName)
        requestData.addData(tableClassName, missingEntries != null)

        val columns = mutableListOf("'${tableClassName.tableName}'.'id'")
        columns.addColumns(tableClassName, missingEntries != null)
        connectedColumn?.let { columns.add("'${tableClassName.tableName}'.'$it'") }

        val tablesAndRestrictions = mutableListOf(tableClassName.tableName)
        tablesAndRestrictions.addJoins(tableClassName, missingEntries != null)
        where?.let { tablesAndRestrictions.add(it) } ?: tablesAndRestrictions.add("WHERE 1")
        restrictions[tableClassName]?.let { tablesAndRestrictions += "AND $it" }

        val query = "SELECT ${columns.joinToString()} FROM ${tablesAndRestrictions.joinToString(" ")};"

        val result = mutableListOf<Pair<Long?, ComplexOrmTable>>()
        database.queryForEach(query) {
            readIndex = 0
            val (connectedId, databaseEntry) = it.readColumns(tableClassName, missingEntries, connectedColumn)
            if (databaseEntry != null) {
                alreadyLoaded.init(tableClassName)[databaseEntry.id!!] = databaseEntry
                result.add(connectedId to databaseEntry)
            }
        }
        alreadyLoadedStart[tableClassName] = alreadyLoaded.init(tableClassName)
        return result
    }

    private fun databaseMapInit(id: Long?) = ComplexOrmTable.init(id)

    private fun RequestData.addData(tableClassName: String, isMissing: Boolean, columnName: String? = null, previousColumn: String? = null) {
        if (givenTables.contains(tableClassName) && !isMissing) return
        val prefix = previousColumn?.plus('$') ?: ""
        normalColumns[tableClassName]?.forEach { columns.add("'${columnName ?: tableClassName.tableName}'.'${it.key}'") }
        connectedColumns[tableClassName]?.forEach { connectedColumnData ->
            if (reverseConnectedColumns[connectedColumnData.value]?.any { it.value.first == tableClassName } == true &&
                alreadyLoadedStart[connectedColumnData.value] == null) return@forEach
            if (alreadyLoadedStart[connectedColumnData.value] != null && !isMissing) {
                columns.add("'${columnName ?: tableClassName.tableName}'.'${connectedColumnData.key}_id'")
                return@forEach
            }
            columns.add("'$prefix${connectedColumnData.key}'.'id'")

            if (alreadyLoadedStart[connectedColumnData.value] != null && !isMissing) return@forEach
            var where = if (connectedColumnData.value == "translation") {
                "LEFT JOIN 'translation' AS '${connectedColumnData.key}' " +
                        "ON '${connectedColumnData.key.removeSuffix("_translation")}_id' = '${connectedColumnData.key}'.'translation_code_id' " +
                        "AND ${(restrictions.getValue("translation")).replace("translation.", "${connectedColumnData.key}.")}"
            } else {
                val joinTableName = connectedColumnData.value.tableName
                "LEFT JOIN '$joinTableName' AS '$prefix${connectedColumnData.key}' " +
                        "ON '$prefix${connectedColumnData.key}'.'id' = '${columnName ?: tableClassName.tableName}'.'${connectedColumnData.key}_id'"
            }
            restrictions[connectedColumnData.value]?.let {
                where = where.removePrefix("LEFT ") + " AND ${it.replace("${connectedColumnData.value}.", "${connectedColumnData.key}.")}"
            }
            tablesAndRestrictions.add(where)

            addData(connectedColumnData.value, isMissing, connectedColumnData.key, prefix + connectedColumnData.key)
        }

    }

    private fun MutableList<String>.addColumns(tableClassName: String, isMissing: Boolean, columnName: String? = null, previousColumn: String? = null) {
        if (alreadyLoadedStart[tableClassName] != null && !isMissing) return
        val prefix = previousColumn?.plus('$') ?: ""
        normalColumns[tableClassName]?.forEach { add("'${columnName ?: tableClassName.tableName}'.'${it.key}'") }
        connectedColumns[tableClassName]?.forEach { joinTable ->
            if (reverseConnectedColumns[joinTable.value]?.any { it.value.first == tableClassName } == true &&
                alreadyLoadedStart[joinTable.value] == null) return@forEach
            if (alreadyLoadedStart[joinTable.value] != null && !isMissing) {
                add("'${columnName ?: tableClassName.tableName}'.'${joinTable.key}_id'")
                return@forEach
            }
            add("'$prefix${joinTable.key}'.'id'")

            addColumns(joinTable.value, isMissing, joinTable.key, prefix + joinTable.key)
        }
    }

    private fun MutableList<String>.addJoins(tableClassName: String, isMissing: Boolean, columnName: String? = null, previousColumn: String? = null) {
        if (alreadyLoadedStart[tableClassName] != null && !isMissing) return
        val prefix = previousColumn?.plus('$') ?: ""
        connectedColumns[tableClassName]?.forEach { joinTable ->
            if (alreadyLoadedStart[joinTable.value] != null && !isMissing) return@forEach
            var where = if (joinTable.value == "translation") {
                "LEFT JOIN 'translation' AS '${joinTable.key}' " +
                        "ON '${joinTable.key.removeSuffix("_translation")}_id' = '${joinTable.key}'.'translation_code_id' " +
                        "AND ${(restrictions.getValue("translation")).replace("translation.", "${joinTable.key}.")}"
            } else {
                val joinTableName = joinTable.value.tableName
                "LEFT JOIN '$joinTableName' AS '$prefix${joinTable.key}' " +
                        "ON '$prefix${joinTable.key}'.'id' = '${columnName ?: tableClassName.tableName}'.'${joinTable.key}_id'"
            }
            restrictions[joinTable.value]?.let {
                where = where.removePrefix("LEFT ") + " AND ${it.replace("${joinTable.value}.", "${joinTable.key}.")}"
            }
            add(where)
            addJoins(joinTable.value, isMissing, joinTable.key, prefix + joinTable.key)
        }
    }

    private fun Cursor.readColumns(
        tableClassName: String,
        missingEntries: List<ComplexOrmTable>?,
        connectedColumn: String? = null
    ): Pair<Long?, ComplexOrmTable?> {
        val id = getValue(readIndex++, ComplexOrmTypes.Long) as Long?

        val databaseMap = databaseMapInit(id)
        val alreadyLoadedTable = alreadyLoadedStart[tableClassName]
        if (alreadyLoadedTable != null && missingEntries == null) {
            val connectedId = connectedColumn?.let { getValue(readIndex++, ComplexOrmTypes.Long) as Long }
            alreadyLoaded[tableClassName]?.get(id)?.let { return connectedId to it }
            return connectedId to (alreadyLoadedTable[id] ?: constructors.getValue(tableClassName).invoke(databaseMap)
                .also { notAlreadyLoaded.init(tableClassName).add(it) })
        }

        normalColumns[tableClassName]?.forEach {
            val value = getValue(readIndex++, it.value)
            databaseMap[it.key.reverseUnderScore] = value
        }
        connectedColumns[tableClassName]?.forEach { joinTableNames ->
            if (reverseConnectedColumns[joinTableNames.value]?.any { it.value.first == tableClassName } == true &&
                alreadyLoadedStart[joinTableNames.value] == null) return@forEach
            val (_, databaseEntry) = readColumns(joinTableNames.value, null)
            if (databaseEntry != null) {
                alreadyLoaded.init(joinTableNames.value)[databaseEntry.id!!] = databaseEntry
                val identifier = joinTableNames.key.reverseUnderScore
                if (databaseMap[identifier] == null) databaseMap[identifier] = databaseEntry
                else return null to databaseMap[identifier] as ComplexOrmTable
            } else databaseMap[joinTableNames.key.reverseUnderScore] = null
        }
        val connectedId = connectedColumn?.let { getValue(readIndex++, ComplexOrmTypes.Long) as Long }

        if (missingEntries == null) alreadyLoaded[tableClassName]?.get(id)?.let { return connectedId to it }

        if (id == null) return connectedId to null
        val databaseEntry = if (missingEntries == null) constructors.getValue(tableClassName).invoke(databaseMap)
        else missingEntries.find { it.id == id }!!.apply { map.putAll(databaseMap) }
        reverseConnectedColumns[tableClassName]?.forEach { _ ->
            nextRequests.init(tableClassName).add(databaseEntry)
        }
        joinColumns[tableClassName]?.asIterable()?.forEach { _ ->
            nextRequests.init(tableClassName).add(databaseEntry)
        }
        reverseJoinColumns[tableClassName]?.forEach { _ ->
            nextRequests.init(tableClassName).add(databaseEntry)
        }
        return connectedId to databaseEntry
    }

    private fun Cursor.getValue(index: Int, type: ComplexOrmTypes): Any? =
        try {
            when (type) {
                ComplexOrmTypes.Boolean -> getInt(index) != 0
                ComplexOrmTypes.Int -> getInt(index)
                ComplexOrmTypes.Long -> getLong(index)
                ComplexOrmTypes.Float -> getFloat(index)
                ComplexOrmTypes.String -> getString(index)
                ComplexOrmTypes.ByteArray -> getBlob(index)
                ComplexOrmTypes.Date -> Date(getLong(index))
                ComplexOrmTypes.LocalDate -> LocalDate.ofEpochDay(getLong(index))
                ComplexOrmTypes.ComplexOrmTable,
                ComplexOrmTypes.ComplexOrmTables -> throw IllegalArgumentException("Shouldn't get table type here")
            }
        } catch (e: Exception) {
            if (isNull(index)) null
            else throw e
        }
}
