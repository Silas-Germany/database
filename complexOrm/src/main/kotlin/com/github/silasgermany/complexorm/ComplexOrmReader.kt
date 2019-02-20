package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass

class ComplexOrmReader(database: ComplexOrmDatabaseInterface) {

    private val complexOrmTableInfo = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface

    private val complexOrmQuery = ComplexOrmQuery(database)

    private val reverseConnectedColumns get() = complexOrmTableInfo.reverseConnectedColumns
    private val joinColumns get() = complexOrmTableInfo.joinColumns
    private val reverseJoinColumns get() = complexOrmTableInfo.reverseJoinColumns
    private val columnNames get() = complexOrmTableInfo.columnNames

    private val String.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(this).first

    inline fun <reified T : ComplexOrmTable> read(
        readTableInfo: ReadTableInfo
    ): List<T> = read(T::class, readTableInfo)

    fun <T : ComplexOrmTable> read(
        table: KClass<T>,
        readTableInfo: ReadTableInfo
    ): List<T> {

        val result = complexOrmQuery.query(table.qualifiedName!!, readTableInfo).map { it.second }

        readTableInfo.notAlreadyLoaded.forEach { (missingEntryTable, missingEntries) ->
            readTableInfo.missingEntries = missingEntries
            val where = "WHERE '$missingEntryTable._id' IN (${readTableInfo.missingEntries!!.joinToString { "${it.id}" }})"
            complexOrmQuery.query(missingEntryTable, readTableInfo, where)
        }

        readTableInfo.nextRequests.forEach { (requestTable, requestEntries) ->
            val ids = requestEntries.map { it.id!! }.toSet().joinToString("")
            val requestTableName = requestTable.tableName

            joinColumns[requestTable]?.forEach { (connectedColumn2, connectedTable) ->
                val connectedTableName = connectedTable.tableName
                val connectedColumn = "${requestTableName}_id"
                val where =
                    "LEFT JOIN '${requestTableName}_$connectedColumn2' AS 'join_table' ON 'join_table'.'${connectedTableName}_id'='$connectedTableName'.'id' " +
                            "WHERE 'join_table'.'${requestTableName}_id' IN ($ids)"
                readTableInfo.connectedColumn = "'join_table'.'$connectedColumn'"
                val joinValues = complexOrmQuery.query(connectedTable, readTableInfo, where)

                readTableInfo.nextRequests[requestTable]!!.forEach { entry ->
                    val id = entry.id!!
                    val columnName = columnNames.getValue(requestTable).getValue(connectedColumn2)
                    entry.map[columnName] = joinValues
                        .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                }
            }
            reverseJoinColumns[requestTable]?.forEach { (connectedColumn2, connectedTableAndColumnName) ->
                val (connectedTable, connectedColumnName) = connectedTableAndColumnName
                val connectedTableName = connectedTable.tableName
                val connectedColumn = "${requestTableName}_id"
                val where =
                    "LEFT JOIN '${connectedTableName}_$connectedColumnName' AS 'reverse_join_table' ON 'reverse_join_table'.'${connectedTableName}_id' = '$connectedTableName'.'id' " +
                            "WHERE 'reverse_join_table'.'$connectedColumn' IN ($ids)"

                readTableInfo.connectedColumn = "'reverse_join_table'.'$connectedColumn'"
                val joinValues = complexOrmQuery.query(connectedTable, readTableInfo, where)

                readTableInfo.nextRequests[requestTable]!!.forEach { entry ->
                    val id = entry.id!!
                    val columnName = columnNames.getValue(requestTable).getValue(connectedColumn2)
                    entry.map[columnName] = joinValues
                        .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                }
            }
            reverseConnectedColumns[requestTable]?.forEach { (connectedColumn, connectedTableAndColumnName) ->
                val (connectedTable, connectedColumnName) = connectedTableAndColumnName
                val connectedTableName = connectedTable.tableName
                val where = "WHERE '$connectedTableName'.'${connectedColumnName}_id' IN ($ids)"

                readTableInfo.connectedColumn = "'$connectedTableName'.'${connectedColumnName}_id' AS 'reverse_connected_column'"
                val joinValues = complexOrmQuery
                    .query(connectedTable, readTableInfo, where)

                val transformedValues = joinValues.groupBy { it.first }
                readTableInfo.nextRequests[requestTable]!!.forEach { entry ->
                    val id = entry.id!!
                    val columnName = columnNames.getValue(requestTable).getValue(connectedColumn)
                    val columnName2 = columnNames.getValue(requestTable).getValue(connectedColumn)
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
        }
        @Suppress("UNCHECKED_CAST")
        return (result as List<T>)
    }
}