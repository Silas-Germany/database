package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.GeneratedSqlTablesInterface
import com.github.silasgermany.complexormapi.SqlAllTables
import com.github.silasgermany.complexormapi.SqlTable
import com.github.silasgermany.complexormapi.SqlTypes
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.reflect.KClass

@SqlAllTables
object SqlWrite: SqlUtils {

    //private val sqlSchema =
      //  Class.forName("com.github.silasgermany.complexorm.GeneratedSqlSchema").getDeclaredField("INSTANCE").get(null) as GeneratedSqlSchemaInterface
    private val sqlTables =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlTables").getDeclaredField("INSTANCE").get(null) as GeneratedSqlTablesInterface

    private fun getIdentifier(tableClass: KClass<*>) = tableClass.java.name
        .run { substring(lastIndexOf('.') + 1).split('$') }.let { it[0] to it[1].sql }

    private fun getNormalColumns(identifier: Pair<String, String>) = identifier.run { sqlTables.normalColumns[first]?.get(second) }
    private fun joinColumns(identifier: Pair<String, String>) = identifier.run { sqlTables.joinColumns[first]?.get(second) }
    private fun connectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.connectedColumns[first]?.get(second) }
    private fun reverseConnectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.reverseConnectedColumns[first]?.get(second) }

    fun write(table: SqlTable): String {
        table.transferId()
        val identifier = getIdentifier(table::class)
        val normalColumns = getNormalColumns(identifier)
        val joinColumns = joinColumns(identifier)
        val connectedColumn = connectedColumn(identifier)
        val reverseConnectedColumn = reverseConnectedColumn(identifier)
        val insertValues = mutableMapOf<String, String>()
        var additionalInserts = ""
        table.map.forEach { (key, value) ->
            val sqlKey = key.sql
            normalColumns?.get(sqlKey)?.let {
                when (it) {
                    SqlTypes.String -> "'$value'"
                    SqlTypes.Int -> value
                    SqlTypes.Boolean -> value
                    SqlTypes.Long -> value
                    SqlTypes.Date -> (value as Date).time
                    SqlTypes.LocalDate -> (value as LocalDate).toEpochDay()
                    SqlTypes.ByteArray -> (value as ByteArray)
                    SqlTypes.SqlTable,
                    SqlTypes.SqlTables -> {
                        throw IllegalArgumentException("Normal table shouldn't have SqlTable inside")
                    }
                }.run { insertValues[key] = toString() }
            }
            joinColumns?.get(sqlKey)?.let { joinTable ->
                try {
                    additionalInserts += "DELETE FROM ${identifier.second}_$sqlKey WHERE ${identifier.second}_id = ${table.id};"
                    (value as List<*>).forEach { joinTableEntry ->
                        additionalInserts += "INSERT INTO ${identifier.second}_$sqlKey(${identifier.second}_id, ${joinTable}_id) VALUES (${table.id}, ${(joinTableEntry as SqlTable).id});"
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save joined table entries: $value")
                }
            }
            if (connectedColumn?.any { it.key == sqlKey } == true) {
                try {
                    val connectedEntry = (value as SqlTable?)
                    if (connectedEntry != null) {
                        if (connectedEntry.id == null) additionalInserts += write(connectedEntry)
                        insertValues["${key}_id"] = connectedEntry.id.toString()
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save connected table entry: $value")
                }
            }
            reverseConnectedColumn?.get(sqlKey)?.let { joinTableData ->
                try {
                    (value as List<*>).forEach { joinTableEntry ->
                        additionalInserts += "INSERT INTO ${joinTableData.first}(${joinTableData.second
                            ?: identifier.second}_id) VALUES (${table.id}) WHERE _id = ${(joinTableEntry as SqlTable).id};"
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save reverse connected table entries: $value")
                }
            }
        }
        return "INSERT INTO ${identifier.first}(${insertValues.keys.joinToString()}) VALUES (${insertValues.values.joinToString()});"
    }
}
