package com.github.silasgermany.complexorm

import android.database.Cursor
import com.github.silasgermany.complexorm.models.AdditionalRequestData
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
    private val columnNames = complexOrmTableInfo.columnNames
    private val normalColumns = complexOrmTableInfo.normalColumns
    private val connectedColumns = complexOrmTableInfo.connectedColumns
    private val reverseConnectedColumns = complexOrmTableInfo.reverseConnectedColumns
    private val joinColumns = complexOrmTableInfo.joinColumns
    private val reverseJoinColumns = complexOrmTableInfo.reverseJoinColumns

    private fun <T, K, V> MutableMap<T, MutableMap<K, V>>.init(key: T) = getOrPut(key) { mutableMapOf() }
    private fun <T, K> MutableMap<T, MutableSet<K>>.init(key: T) = getOrPut(key) { mutableSetOf() }
    private val String.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(this).first
    private fun MutableMap<String, Any?>.createClass(tableClassName: String) =
        takeUnless { it["id"] == null }?.let { constructors.getValue(tableClassName).invoke(it) }

    fun query(
        tableClassName: String,
        additionalRequestData: AdditionalRequestData,
        where: String? = null
    ): List<Pair<Long?, ComplexOrmTable>> {

        val requestData = RequestData(tableClassName.tableName, tableClassName, where, additionalRequestData)
        requestData.addData(tableClassName)

        val result: MutableList<Pair<Long?, ComplexOrmTable>> = mutableListOf()
        database.queryForEach(requestData.query) {
            readIndex = 0
            val (connectedId, databaseEntry) = readColumns(tableClassName, it, additionalRequestData)
            if (databaseEntry != null) {
                additionalRequestData.setTable(tableClassName, databaseEntry)
                result.add(connectedId to databaseEntry)
            }
        }
        return result
    }

    private fun RequestData.addData(tableClassName: String, previousColumn: String? = null) {
        if (additionalRequestData.alreadyGiven(tableClassName)) return
        val tableName = previousColumn ?: tableClassName.tableName
        val prefix = previousColumn?.plus('$') ?: ""
        normalColumns[tableClassName]?.forEach {
            columns.add("'$tableName'.'${it.key}'")
        }
        connectedColumns[tableClassName]?.forEach { (connectedColumnName, connectedTableClassName) ->
            if (isReverselyLoaded(tableClassName, connectedColumnName, additionalRequestData)) return@forEach
            if (additionalRequestData.alreadyGiven(connectedColumnName)) {
                columns.add("'$tableName'.'${connectedTableClassName}_id'")
                return@forEach
            }
            val connectedTableName = connectedTableClassName.tableName
            val newConnectedTableName = "$prefix$connectedColumnName"

            columns.add("'$newConnectedTableName'.'id'")

            var where = "LEFT JOIN '$connectedTableName' AS '$newConnectedTableName' " +
                    "ON '$newConnectedTableName'.'id'='$tableName'.'${connectedColumnName}_id'"
            additionalRequestData.restrictions[connectedTableClassName]?.let {
                where = where.removePrefix("LEFT ") + " AND ${it.replace("'$connectedTableClassName'.", "'$connectedTableName'.")}"
            }
            tablesAndRestrictions.add(where)

            addData(connectedTableClassName, newConnectedTableName)
        }
    }

    private fun isReverselyLoaded(tableClassName: String, connectedColumnName: String, additionalRequestData: AdditionalRequestData) =
        reverseConnectedColumns[connectedColumnName]
            ?.any { it.value.run { first == tableClassName && second == connectedColumnName } } == true &&
                additionalRequestData.has(connectedColumnName)

    private fun readColumns(tableClassName: String, cursor: Cursor, additionalRequestData: AdditionalRequestData): Pair<Long?, ComplexOrmTable?> {
        val id = cursor.getValue(readIndex++, ComplexOrmTypes.Long) as Long?

        val databaseMap = ComplexOrmTable.init(id)
        if (additionalRequestData.alreadyGiven(tableClassName)) {
            val connectedId = additionalRequestData.connectedColumn?.let { cursor.getValue(readIndex++, ComplexOrmTypes.Long) as Long }
            additionalRequestData.getTable(tableClassName, id)?.let { return connectedId to it }
            val table = databaseMap.createClass(tableClassName)
            additionalRequestData.addRequest(tableClassName, table)
            return connectedId to table
        }

        normalColumns[tableClassName]?.forEach {
            val columnName = columnNames.getValue(tableClassName).getValue(it.key)
            databaseMap[columnName] = cursor.getValue(readIndex++, it.value)
        }

        connectedColumns[tableClassName]
            ?.filter {
                !isReverselyLoaded(tableClassName, it.key, additionalRequestData) &&
                        (!additionalRequestData.alreadyGiven(it.value))
            }?.forEach { (connectedColumnName, connectedTableClassName) ->
                val columnName = columnNames.getValue(tableClassName).getValue(connectedColumnName)
                val (_, databaseEntry) = readColumns(connectedTableClassName, cursor, additionalRequestData)
                if (databaseEntry != null) {
                    additionalRequestData.setTable(connectedTableClassName, databaseEntry)
                    System.out.println("Problem: $databaseMap")
                    if (columnName !in databaseMap) databaseMap[columnName] = databaseEntry
                    else return null to databaseMap.getValue(columnName) as ComplexOrmTable
                } else databaseMap[columnName] = null
            }
        val connectedId = additionalRequestData.connectedColumn?.let { cursor.getValue(readIndex++, ComplexOrmTypes.Long) as Long }

        if (!additionalRequestData.isMissingRequest)
            additionalRequestData.getTable(tableClassName, id)?.let { return connectedId to it }

        if (id == null) return connectedId to null
        val databaseEntry = if (!additionalRequestData.isMissingRequest) databaseMap.createClass(tableClassName)!!
        else additionalRequestData.missingEntries!!.find { it.id == id }!!.apply { map.putAll(databaseMap) }
        when(tableClassName) {
            in reverseConnectedColumns,
            in joinColumns,
            in reverseJoinColumns -> additionalRequestData.addRequest(tableClassName, databaseEntry)
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
