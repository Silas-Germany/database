package com.github.silasgermany.complexorm

import android.database.Cursor
import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexorm.models.RequestInfo
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ComplexOrmQuery internal constructor(private val database: ComplexOrmDatabaseInterface,
                      private val complexOrmTableInfo: ComplexOrmTableInfoInterface) {

    private val UUID.asSql get() = "x'${toString().replace("-", "")}'"

    private val String.asUuid: UUID get() = UUID.fromString(replace(
        "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(),
        "$1-$2-$3-$4-$5"
    ))

    fun <T: ComplexOrmTable, R, V: Any>getOneColumn(table: KClass<T>, column: KProperty1<T, R>, id: UUID, returnClass: KClass<V>): V? {
        return database.queryMap("SELECT ${column.name.toSql()} FROM ${table.tableName} WHERE id = ${id.asSql}") {
            @Suppress("UNCHECKED_CAST")
            if (it.isNull(0)) null
            else when (returnClass) {
                Boolean::class -> (it.getInt(0) == 0) as V
                Int::class -> it.getInt(0) as V
                Long::class -> it.getLong(0) as V
                String::class -> it.getString(0) as V
                else -> throw IllegalArgumentException("Doesn't have type ${returnClass.java.simpleName}")
            }
        }.firstOrNull()
    }

    private fun MutableMap<String, Any?>.createClass(tableClassName: String) =
            complexOrmTableInfo.tableConstructors.getValue(tableClassName).invoke(this)

    fun query(
            tableClassName: String,
            readTableInfo: ReadTableInfo,
            where: String? = null
    ): List<Pair<UUID?, ComplexOrmTable>> {
        val requestInfo = RequestInfo(readTableInfo.getBasicTableInfoFirstValue(tableClassName), tableClassName, where, readTableInfo)
        requestInfo.addData(tableClassName)

        val result: MutableList<Pair<UUID?, ComplexOrmTable>> = mutableListOf()
        database.queryForEach(requestInfo.query) {
            readTableInfo.readIndex = 0
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
        val tableName = previousColumn ?: readTableInfo.getBasicTableInfoFirstValue(tableClassName)
        val prefix = previousColumn?.plus('$') ?: ""
        readTableInfo.getNormalColumnsValue(tableClassName).forEach {
            columns.add("\"$tableName\".\"${it.key}\"")
        }
        readTableInfo.getConnectedColumnsValue(tableClassName).forEach { (connectedColumnName, connectedTableClassName) ->
            if (isReverselyLoaded(tableClassName, connectedColumnName, readTableInfo)) return@forEach
            if (readTableInfo.alreadyGiven(connectedTableClassName)) {
                columns.add("hex(\"$tableName\".\"${connectedColumnName}_id\")")
                return@forEach
            }
            val connectedTableName = readTableInfo.getBasicTableInfoFirstValue(connectedTableClassName)
            val newConnectedTableName = "$prefix$connectedColumnName"

            columns.add("hex(\"$newConnectedTableName\".\"id\")")

            var where = "LEFT JOIN \"$connectedTableName\" AS \"$newConnectedTableName\" " +
                    "ON \"$newConnectedTableName\".\"id\"=\"$tableName\".\"${connectedColumnName}_id\""
            readTableInfo.restrictions[connectedTableClassName]?.let {
                where = where.removePrefix("LEFT ") + " AND ${it.replace("$$", newConnectedTableName)}"
            }
            tablesAndRestrictions.add(where)

            readTableInfo.loadingTables.add(tableClassName)
            addData(connectedTableClassName, newConnectedTableName)
            readTableInfo.loadingTables.remove(tableClassName)
        }
        readTableInfo.getSpecialConnectedColumnsValue(tableClassName).forEach { (connectedColumnName, connectedTableInfo) ->
            val (connectedTableClassName, connectedTableColumn) = connectedTableInfo.split(';').let { it[0] to it[1] }
            if (isReverselyLoaded(tableClassName, connectedColumnName, readTableInfo)) return@forEach
            if (readTableInfo.alreadyGiven(connectedTableClassName)) {
                columns.add("hex(\"$tableName\".\"${connectedColumnName}_id\")")
                return@forEach
            }
            val connectedTableName = readTableInfo.getBasicTableInfoFirstValue(connectedTableClassName)
            val newConnectedTableName = "$prefix$connectedColumnName"

            columns.add("hex(\"$newConnectedTableName\".\"id\")")

            var where = "LEFT JOIN \"$connectedTableName\" AS \"$newConnectedTableName\" " +
                    "ON \"$newConnectedTableName\".\"$connectedTableColumn\"=\"$tableName\".\"${connectedColumnName}_id\""
            readTableInfo.restrictions[connectedTableClassName]?.let {
                where = where.removePrefix("LEFT ") + " AND ${it.replace("$$", "\"$newConnectedTableName\"")}"
            }
            tablesAndRestrictions.add(where)

            readTableInfo.loadingTables.add(tableClassName)
            addData(connectedTableClassName, newConnectedTableName)
            readTableInfo.loadingTables.remove(tableClassName)
        }
    }

    private fun isReverselyLoaded(tableClassName: String, connectedColumnName: String, readTableInfo: ReadTableInfo) =
            readTableInfo.getReverseConnectedColumnsValue(tableClassName)
                    .any { it.value == "$tableClassName;$connectedColumnName" } &&
                    readTableInfo.has(connectedColumnName)

    private fun readColumns(tableClassName: String, cursor: Cursor, readTableInfo: ReadTableInfo,
                            connectedColumn: String?, specialConnectedColumn: String? = null): Pair<UUID?, ComplexOrmTable?> {
        val id = (cursor.getValue(readTableInfo.readIndex++, "String") as String?)?.asUuid

        if (readTableInfo.alreadyGiven(tableClassName) || readTableInfo.alreadyLoading(tableClassName)) {
            val connectedId = connectedColumn?.let { cursor.getValue(readTableInfo.readIndex++, "String") as String }?.asUuid
            readTableInfo.getTable(tableClassName, id)?.let { return connectedId to it }
            id ?: return connectedId to null
            val databaseMap = ComplexOrmTable.default
            databaseMap[specialConnectedColumn ?: "id"] = id
            val table = databaseMap.createClass(tableClassName)
            readTableInfo.addMissingTable(tableClassName, table)
            return connectedId to table
        }
        val databaseMap = ComplexOrmTable.init(id)

        val order = readTableInfo.getNormalColumnsValue(tableClassName)
        order.forEach {
            val columnName = readTableInfo.getColumnNamesValue(tableClassName).getValue(it.key)
            databaseMap[columnName] = cursor.getValue(readTableInfo.readIndex++, it.value)
        }

        val handleConnectedColumns: (String, String, String?) -> Pair<UUID?, ComplexOrmTable?>? = handleConnectedColumns@{ connectedColumnName, connectedTableClassName, specialConnectedColumnName ->
            if (isReverselyLoaded(tableClassName, connectedColumnName, readTableInfo)) return@handleConnectedColumns null
            val columnName = readTableInfo.getColumnNamesValue(tableClassName).getValue(connectedColumnName)
            val specialColumnName = specialConnectedColumnName?.let {
                readTableInfo.getColumnNamesValue(readTableInfo.getBasicTableInfoSecondValue(connectedTableClassName)).getValue(it.removeSuffix("_id"))
            }
            readTableInfo.loadingTables.add(tableClassName)
            val (_, databaseEntry) = readColumns(connectedTableClassName, cursor, readTableInfo, null, specialColumnName)
            readTableInfo.loadingTables.remove(tableClassName)
            if (databaseEntry != null) {
                readTableInfo.setTable(connectedTableClassName, databaseEntry, specialColumnName)
                if (columnName !in databaseMap) databaseMap[columnName] = databaseEntry
                else {
                    val connectedId = connectedColumn?.let { cursor.getValue(readTableInfo.readIndex++, "String") as String }?.asUuid
                    return@handleConnectedColumns connectedId to databaseMap.getValue(columnName) as ComplexOrmTable // (Not sure, why it has to return here)
                }
            } else databaseMap[columnName] = null
            return@handleConnectedColumns null
        }
        readTableInfo.getConnectedColumnsValue(tableClassName).forEach { (connectedColumnName, connectedTableClassName) ->
            val result = handleConnectedColumns(connectedColumnName, connectedTableClassName, null)
            if (result != null) return result
        }

        readTableInfo.getSpecialConnectedColumnsValue(tableClassName).forEach { (connectedColumnName, connectedTableInfo) ->
            val (connectedTableClassName, connectingColumnName) = connectedTableInfo.split(';').let { it[0] to it[1] }
            val result = handleConnectedColumns(connectedColumnName, connectedTableClassName, connectingColumnName)
            if (result != null) return result
        }

        val connectedId = connectedColumn?.let { cursor.getValue(readTableInfo.readIndex++, "String") as String? }?.asUuid

        if (!readTableInfo.isMissingRequest(tableClassName))
            readTableInfo.getTable(tableClassName, id)?.let { return connectedId to it }

        if (id == null) return connectedId to null
        val databaseEntry = if (!readTableInfo.isMissingRequest(tableClassName)) databaseMap.createClass(tableClassName)
        else {
            val rootTableClassName = readTableInfo.getBasicTableInfoSecondValue(tableClassName)
            val specialConnectingColumn = connectedColumn?.takeUnless { it.endsWith(".\"id\"") }?.let {
                readTableInfo.getColumnNamesValue(rootTableClassName).getValue(it.removeSuffix("\"").split('"').last().removeSuffix("_id"))
            } ?: "id"
            val connectingId = if (specialConnectedColumn == "id") id else connectedId
            readTableInfo.missingEntries!!.find {
                it.javaClass.canonicalName == tableClassName &&
                        it.map[specialConnectingColumn] == connectingId
            }?.apply { map.putAll(databaseMap) }
                    ?: throw IllegalStateException("Couldn't find $tableClassName and $connectingId in ${readTableInfo.missingEntries}")
        }
        if (readTableInfo.getReverseConnectedColumnsValue(tableClassName).isNotEmpty()
                || readTableInfo.getJoinColumnsValue(tableClassName).isNotEmpty()
                || readTableInfo.getReverseJoinColumnsValue(tableClassName).isNotEmpty()) {
            readTableInfo.addRequest(tableClassName, databaseEntry)
        }
        return connectedId to databaseEntry
    }

    private fun Cursor.getValue(index: Int, type: String): Any? {
        return if (isNull(index)) null
        else when (type.asType) {
            ComplexOrmTypes.Boolean -> getInt(index) != 0
            ComplexOrmTypes.Int -> getInt(index)
            ComplexOrmTypes.Long -> getLong(index)
            ComplexOrmTypes.Float -> getFloat(index)
            ComplexOrmTypes.String -> getString(index)
            ComplexOrmTypes.ByteArray -> getBlob(index)
            ComplexOrmTypes.Date -> Date(getLong(index))
            ComplexOrmTypes.LocalDate -> LocalDate.parse(
                getString(index),
                DateTimeFormat.forPattern("yyyy-MM-dd")
            )
            ComplexOrmTypes.Uuid -> UUID.fromString(
                getString(index).replace(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(),
                    "\\L$1-$2-$3-$4-$5"
                )
            )
        }
    }
}
