package com.github.silasgermany.complexorm

import android.database.Cursor
import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexorm.models.RequestInfo
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
    private val basicTableInfo = complexOrmTableInfo.basicTableInfo
    private val columnNames = complexOrmTableInfo.columnNames
    private val normalColumns = complexOrmTableInfo.normalColumns
    private val connectedColumns = complexOrmTableInfo.connectedColumns
    private val reverseConnectedColumns = complexOrmTableInfo.reverseConnectedColumns
    private val joinColumns = complexOrmTableInfo.joinColumns
    private val reverseJoinColumns = complexOrmTableInfo.reverseJoinColumns
    private val specialConnectedColumns = complexOrmTableInfo.specialConnectedColumns

    private val String.tableName get() = basicTableInfo.getValue(this).first
    private fun MutableMap<String, Any?>.createClass(tableClassName: String) =
        constructors.getValue(tableClassName).invoke(this)

    fun query(
        tableClassName: String,
        readTableInfo: ReadTableInfo,
        where: String? = null
    ): List<Pair<Long?, ComplexOrmTable>> {
        val requestInfo = RequestInfo(tableClassName.tableName, tableClassName, where, readTableInfo)
        requestInfo.addData(tableClassName)

        val result: MutableList<Pair<Long?, ComplexOrmTable>> = mutableListOf()
        database.queryForEach(requestInfo.query) {
            readIndex = 0
            val (connectedId, databaseEntry) = readColumns(tableClassName, it, readTableInfo, readTableInfo.connectedColumn)
            if (databaseEntry != null) {
                readTableInfo.setTable(tableClassName, databaseEntry)
                result.add(connectedId to databaseEntry)
            }
        }
        return result
    }

    private fun RequestInfo.addData(tableClassName: String, previousColumn: String? = null) {
        if (readTableInfo.alreadyGiven(tableClassName) || readTableInfo.alreadyLoading(tableClassName)) return
        val tableName = previousColumn ?: tableClassName.tableName
        val prefix = previousColumn?.plus('$') ?: ""
        normalColumns[tableClassName]?.forEach {
            columns.add("\"$tableName\".\"${it.key}\"")
        }
        connectedColumns[tableClassName]?.forEach { (connectedColumnName, connectedTableClassName) ->
            if (isReverselyLoaded(tableClassName, connectedColumnName, readTableInfo)) return@forEach
            if (readTableInfo.alreadyGiven(connectedTableClassName)) {
                columns.add("\"$tableName\".\"${connectedColumnName}_id\"")
                return@forEach
            }
            val connectedTableName = connectedTableClassName.tableName
            val newConnectedTableName = "$prefix$connectedColumnName"

            columns.add("\"$newConnectedTableName\".\"id\"")

            var where = "LEFT JOIN \"$connectedTableName\" AS \"$newConnectedTableName\" " +
                    "ON \"$newConnectedTableName\".\"id\"=\"$tableName\".\"${connectedColumnName}_id\""
            readTableInfo.restrictions[connectedTableClassName]?.let {
                where = where.removePrefix("LEFT ") + " AND ${it.replace("??", newConnectedTableName)}"
            }
            tablesAndRestrictions.add(where)

            readTableInfo.loadingTables.add(tableClassName)
            addData(connectedTableClassName, newConnectedTableName)
            readTableInfo.loadingTables.remove(tableClassName)
        }
        specialConnectedColumns[tableClassName]?.forEach { (connectedColumnName, connectedTableInfo) ->
            val (connectedTableClassName, connectedTableColumn) = connectedTableInfo
            if (isReverselyLoaded(tableClassName, connectedColumnName, readTableInfo)) return@forEach
            if (readTableInfo.alreadyGiven(connectedTableClassName)) {
                columns.add("\"$tableName\".\"${connectedColumnName}_id\"")
                return@forEach
            }
            val connectedTableName = connectedTableClassName.tableName
            val newConnectedTableName = "$prefix$connectedColumnName"

            columns.add("\"$newConnectedTableName\".\"id\"")

            var where = "LEFT JOIN \"$connectedTableName\" AS \"$newConnectedTableName\" " +
                    "ON \"$newConnectedTableName\".\"$connectedTableColumn\"=\"$tableName\".\"${connectedColumnName}_id\""
            readTableInfo.restrictions[connectedTableClassName]?.let {
                where = where.removePrefix("LEFT ") + " AND ${it.replace("??", newConnectedTableName)}"
            }
            tablesAndRestrictions.add(where)

            readTableInfo.loadingTables.add(tableClassName)
            addData(connectedTableClassName, newConnectedTableName)
            readTableInfo.loadingTables.remove(tableClassName)
        }
    }

    private fun isReverselyLoaded(tableClassName: String, connectedColumnName: String, readTableInfo: ReadTableInfo) =
        reverseConnectedColumns[connectedColumnName]
            ?.any { it.value.run { first == tableClassName && second == connectedColumnName } } == true &&
                readTableInfo.has(connectedColumnName)

    private fun readColumns(tableClassName: String, cursor: Cursor, readTableInfo: ReadTableInfo,
                            connectedColumn: String?, specialConnectedColumn: String? = null): Pair<Long?, ComplexOrmTable?> {
        val id = cursor.getValue(readIndex++, ComplexOrmTypes.Long) as Long?

        if (readTableInfo.alreadyGiven(tableClassName) || readTableInfo.alreadyLoading(tableClassName)) {
            val connectedId = connectedColumn?.let { cursor.getValue(readIndex++, ComplexOrmTypes.Long) as Long }
            readTableInfo.getTable(tableClassName, id)?.let { return connectedId to it }
            id ?: return connectedId to null
            val databaseMap = ComplexOrmTable.default
            databaseMap[specialConnectedColumn ?: "id"] = id
            val table = databaseMap.createClass(tableClassName)
            readTableInfo.addMissingTable(tableClassName, table)
            return connectedId to table
        }
        val databaseMap = ComplexOrmTable.init(id)

        normalColumns[tableClassName]?.forEach {
            val columnName = columnNames.getValue(tableClassName).getValue(it.key)
            databaseMap[columnName] = cursor.getValue(readIndex++, it.value)
        }

        val handleConnectedColumns: (String, String, String?) -> Pair<Long?, ComplexOrmTable?>? = handleConnectedColumns@{ connectedColumnName, connectedTableClassName, specialConnectedColumnName ->
            if (isReverselyLoaded(tableClassName, connectedColumnName, readTableInfo)) return@handleConnectedColumns null
            val columnName = columnNames.getValue(tableClassName).getValue(connectedColumnName)
            val specialColumnName = specialConnectedColumnName?.let {
                columnNames.getValue(basicTableInfo.getValue(connectedTableClassName).second).getValue(it.removeSuffix("_id"))
            }
            readTableInfo.loadingTables.add(tableClassName)
            val (_, databaseEntry) = readColumns(connectedTableClassName, cursor, readTableInfo, null, specialColumnName)
            readTableInfo.loadingTables.remove(tableClassName)
            if (databaseEntry != null) {
                readTableInfo.setTable(connectedTableClassName, databaseEntry, specialColumnName)
                if (columnName !in databaseMap) databaseMap[columnName] = databaseEntry
                else {
                    val connectedId = connectedColumn?.let { cursor.getValue(readIndex++, ComplexOrmTypes.Long) as Long }
                    return@handleConnectedColumns connectedId to databaseMap.getValue(columnName) as ComplexOrmTable
                }
            } else databaseMap[columnName] = null
            return@handleConnectedColumns null
        }
        connectedColumns[tableClassName]?.forEach { (connectedColumnName, connectedTableClassName) ->
            val result = handleConnectedColumns(connectedColumnName, connectedTableClassName, null)
            if (result != null) return result
        }

        specialConnectedColumns[tableClassName]?.forEach { (connectedColumnName, connectedTableInfo) ->
            val connectedTableClassName = connectedTableInfo.first
            val connectingColumnName = connectedTableInfo.second
            val result = handleConnectedColumns(connectedColumnName, connectedTableClassName, connectingColumnName)
            if (result != null) return result
        }

        val connectedId = connectedColumn?.let { cursor.getValue(readIndex++, ComplexOrmTypes.Long) as Long }

        if (!readTableInfo.isMissingRequest)
            readTableInfo.getTable(tableClassName, id)?.let { return connectedId to it }

        if (id == null) return connectedId to null
        val databaseEntry = if (!readTableInfo.isMissingRequest) databaseMap.createClass(tableClassName)
        else {
            val rootTableClassName = basicTableInfo.getValue(tableClassName).second
            val specialConnectingColumn = connectedColumn?.let {
                columnNames.getValue(rootTableClassName).getValue(it.removeSuffix("_id"))
            } ?: "id"
            readTableInfo.missingEntries!!.find {
                it::class.java.canonicalName == tableClassName &&
                it.map[specialConnectingColumn] == connectedId
            }!!.apply { map.putAll(databaseMap) }
        }
        when(tableClassName) {
            in reverseConnectedColumns,
            in joinColumns,
            in reverseJoinColumns -> readTableInfo.addRequest(tableClassName, databaseEntry)
        }
        return connectedId to databaseEntry
    }

    private fun Cursor.getValue(index: Int, type: ComplexOrmTypes): Any? {
        return if (isNull(index)) null
        else when (type) {
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
    }
}
