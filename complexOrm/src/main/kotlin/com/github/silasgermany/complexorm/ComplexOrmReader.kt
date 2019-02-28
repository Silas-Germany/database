package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import java.io.File
import kotlin.reflect.KClass

class ComplexOrmReader internal constructor(database: ComplexOrmDatabaseInterface, private val cacheDir: File? = null,
                       complexOrmTableInfo: ComplexOrmTableInfoInterface) {

    val complexOrmQuery = ComplexOrmQuery(database, complexOrmTableInfo)

    private fun ReadTableInfo.getTableName(tableClassName: String) = getBasicTableInfoFirstValue(tableClassName)

    inline fun <reified T : ComplexOrmTable> read(
            readTableInfo: ReadTableInfo
    ): List<T> = read(T::class, readTableInfo)

    fun <T : ComplexOrmTable> read(
            table: KClass<T>,
            readTableInfo: ReadTableInfo
    ): List<T> {
        if (cacheDir != null) readTableInfo.initFromCache(File(cacheDir, "complex_orm_$table"))
        val result = complexOrmQuery.query(table.java.canonicalName!!, readTableInfo).map { it.second }

        while (readTableInfo.notAlreadyLoaded.isNotEmpty() || readTableInfo.nextRequests.isNotEmpty()) {
            while (readTableInfo.notAlreadyLoaded.isNotEmpty()) {
                val missingEntryTable = readTableInfo.notAlreadyLoaded.keys.first()
                val missingEntries = readTableInfo.notAlreadyLoaded.getValue(missingEntryTable)
                readTableInfo.missingEntries = missingEntries
                val column = missingEntries.first().map.entries
                        .filter { it.value != null }
                        .takeIf { it.size == 1 }?.first()?.key
                        ?: throw IllegalStateException("Should have exactly one entry")
                val columnName = column.replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2")
                        .toLowerCase().takeUnless { it == "id" }?.plus("_id") ?: "id"
                val fullColumnName = "\"${readTableInfo.getTableName(missingEntryTable)}\".\"$columnName\""
                val where = "WHERE $fullColumnName IN (${missingEntries.joinToString { "${it.map[column]}" }})"
                readTableInfo.connectedColumn = fullColumnName
                complexOrmQuery.query(missingEntryTable, readTableInfo, where)
                readTableInfo.notAlreadyLoaded.remove(missingEntryTable)
            }
            readTableInfo.missingEntries = null

            while (readTableInfo.nextRequests.isNotEmpty()) {
                val requestTable = readTableInfo.nextRequests.keys.first()
                val requestEntries = readTableInfo.nextRequests.getValue(requestTable)
                val ids = requestEntries.map { it.id!! }.toSet().joinToString(",")
                val requestTableName = readTableInfo.getTableName(requestTable)

                readTableInfo.getJoinColumnsValue(requestTable).forEach { (connectedColumn2, connectedTable) ->
                    val connectedTableName = readTableInfo.getTableName(connectedTable)
                    val connectedColumn = "${requestTableName}_id"
                    val where =
                            "LEFT JOIN \"${requestTableName}_$connectedColumn2\" AS \"join_table\" ON \"join_table\".\"${connectedTableName}_id\"=\"$connectedTableName\".\"id\" " +
                                    "WHERE \"join_table\".\"${requestTableName}_id\" IN ($ids)"
                    readTableInfo.connectedColumn = "\"join_table\".\"$connectedColumn\""
                    val joinValues = complexOrmQuery.query(connectedTable, readTableInfo, where)

                    readTableInfo.nextRequests[requestTable]!!.forEach { entry ->
                        val id = entry.id!!
                        val columnName = readTableInfo.getColumnNamesValue(requestTable).getValue(connectedColumn2)
                        entry.map[columnName] = joinValues
                                .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                    }
                }
                readTableInfo.getReverseJoinColumnsValue(requestTable).forEach { (connectedColumn2, connectedTableAndColumnName) ->
                    val (connectedTable, connectedColumnName) = connectedTableAndColumnName.split(';').let { it[0] to it[1] }
                    val connectedTableName = readTableInfo.getTableName(connectedTable)
                    val connectedColumn = "${requestTableName}_id"
                    val where =
                            "LEFT JOIN \"${connectedTableName}_$connectedColumnName\" AS \"reverse_join_table\" ON \"reverse_join_table\".\"${connectedTableName}_id\" = \"$connectedTableName\".\"id\" " +
                                    "WHERE \"reverse_join_table\".\"$connectedColumn\" IN ($ids)"

                    readTableInfo.connectedColumn = "\"reverse_join_table\".\"$connectedColumn\""
                    val joinValues = complexOrmQuery.query(connectedTable, readTableInfo, where)

                    readTableInfo.nextRequests[requestTable]!!.forEach { entry ->
                        val id = entry.id!!
                        val columnName = readTableInfo.getColumnNamesValue(requestTable).getValue(connectedColumn2)
                        entry.map[columnName] = joinValues
                                .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                    }
                }
                readTableInfo.getReverseConnectedColumnsValue(requestTable).forEach { (connectedColumn, connectedTableAndColumnName) ->
                    val (connectedTable, connectedColumnName) = connectedTableAndColumnName.split(';').let { it[0] to it[1] }
                    val connectedTableName = readTableInfo.getTableName(connectedTable)
                    val where = "WHERE \"$connectedTableName\".\"${connectedColumnName}_id\" IN ($ids)"

                    readTableInfo.connectedColumn = "\"$connectedTableName\".\"${connectedColumnName}_id\" AS \"reverse_connected_column\""
                    val joinValues = complexOrmQuery
                            .query(connectedTable, readTableInfo, where)

                    val transformedValues = joinValues.groupBy { it.first }
                    readTableInfo.nextRequests[requestTable]!!.forEach { entry ->
                        val id = entry.id!!
                        val columnName = readTableInfo.getColumnNamesValue(requestTable).getValue(connectedColumn)
                        val columnName2 = readTableInfo.getColumnNamesValue(requestTable).getValue(connectedColumn)
                        entry.map[columnName] = transformedValues[id]?.map { value ->
                            value.second.map[columnName2] = entry
                            value.second
                        }.orEmpty()
                        entry.map[columnName] = joinValues.mapNotNull { joinEntry ->
                            joinEntry.second.takeIf { joinEntry.first == id }
                                    ?.apply { map[columnName2] = entry }
                        }
                    }
                }
                readTableInfo.nextRequests.remove(requestTable)
            }
        }
        readTableInfo.writeToCache()
        @Suppress("UNCHECKED_CAST")
        return (result as List<T>)
    }
}