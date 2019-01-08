package com.github.silasgermany.complexorm

import android.content.ContentValues
import android.util.Log
import com.github.silasgermany.complexormapi.GeneratedSqlSchemaInterface
import com.github.silasgermany.complexormapi.GeneratedSqlTablesInterface
import com.github.silasgermany.complexormapi.SqlTable
import com.github.silasgermany.complexormapi.SqlTypes
import org.threeten.bp.LocalDate
import java.io.File
import java.util.*
import kotlin.reflect.KClass

class SqlWriter(private val database: SqlDatabase): SqlUtils() {

    constructor(databaseFile: File) : this(SqlDatabase(databaseFile))

    private val sqlSchema =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlSchema")
            .getDeclaredField("INSTANCE").get(null) as GeneratedSqlSchemaInterface
    private val sqlTables =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlTables")
            .getDeclaredField("INSTANCE").get(null) as GeneratedSqlTablesInterface

    private fun getIdentifier(tableClass: KClass<*>) = tableClass.java.name
        .run { substring(lastIndexOf('.') + 1).split('$') }.let { it[0] to it[1].sql }

    private fun getNormalColumns(identifier: Pair<String, String>) = mapOf("_id" to SqlTypes.Long).plus(identifier.run { sqlTables.normalColumns[first]?.get(second) } ?: emptyMap())
    private fun joinColumns(identifier: Pair<String, String>) = identifier.run { sqlTables.joinColumns[first]?.get(second) }
    private fun connectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.connectedColumns[first]?.get(second) }
    private fun reverseConnectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.reverseConnectedColumns[first]?.get(second) }

    fun write(table: SqlTable): String {
        return database.use {
                sqlSchema.dropTableCommands.forEach { rawSql(it) }
                sqlSchema.createTableCommands.forEach { rawSql(it) }
            write2(table)
        }
    }

    private fun SqlDatabase.write2(table: SqlTable): String {
        Log.e("DATABASE", "Save $table")
        val contentValues = ContentValues()
        val identifier = getIdentifier(table::class)
        val normalColumns = getNormalColumns(identifier)
        val joinColumns = joinColumns(identifier)
        val connectedColumn = connectedColumn(identifier)
        val reverseConnectedColumn = reverseConnectedColumn(identifier)
        table.map.forEach { (key, value) ->
            val sqlKey = key.sql
            normalColumns.get(sqlKey)?.also {
                if (value == null) contentValues.putNull(key)
                else when (it) {
                    SqlTypes.String -> contentValues.put(key, value as String)
                    SqlTypes.Int -> contentValues.put(key, value as Int)
                    SqlTypes.Boolean -> contentValues.put(key, value as Boolean)
                    SqlTypes.Long -> contentValues.put(key, value as Long)
                    SqlTypes.Float -> contentValues.put(key, value as Float)
                    SqlTypes.Date -> contentValues.put(key, (value as Date).time)
                    SqlTypes.LocalDate -> contentValues.put(key, (value as LocalDate).toEpochDay())
                    SqlTypes.ByteArray -> contentValues.put(key, value as ByteArray)
                    SqlTypes.SqlTable,
                    SqlTypes.SqlTables -> {
                        throw IllegalArgumentException("Normal table shouldn't have SqlTable inside")
                    }
                }.let { } // this is checking, that the when is exhaustive
            }
            connectedColumn?.get(sqlKey)?.let {
                try {
                    val connectedEntry = (value as SqlTable?)
                    if (connectedEntry != null) {
                        if (connectedEntry.id == null) write2(connectedEntry)
                        contentValues.put("${key.sql}_id", connectedEntry.id.toString())
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save connected table entry: $value (${e.message})")
                }
            }
        }
        try {
            table.map["_id"] = save(identifier.second, contentValues)
        } catch (e: Exception) {
            throw IllegalArgumentException("Couldn't save reverse connected table entries: $table (${e.message})")
        }
        table.map.forEach { (key, value) ->
            val sqlKey = key.sql
            joinColumns?.get(sqlKey)?.let { joinTable ->
                try {
                    delete("${identifier.second}_$sqlKey", "${identifier.second}_id = ${table.id}")
                    val innerContentValues = ContentValues()
                    innerContentValues.put("${identifier.second}_id", table.id)
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as SqlTable
                        if (joinTableEntry.id == null) write2(joinTableEntry)
                        innerContentValues.put("${joinTable}_id", joinTableEntry.id)
                        save("${identifier.second}_$sqlKey", innerContentValues)
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save joined table entries: $value (${e.message})")
                }
            }
            reverseConnectedColumn?.get(sqlKey)?.let { joinTableData ->
                try {
                    val innerContentValues = ContentValues()
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as SqlTable
                        if (joinTableEntry.id == null) write2(joinTableEntry)
                        innerContentValues.put("${joinTableData.second ?: identifier.second}_id", table.id)
                        update(joinTableData.first, innerContentValues, "_id = ${joinTableEntry.id}")
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save reverse connected table entries: $value (${e.message})")
                }
            }
        }
        return table.toString()
    }

    private fun SqlDatabase.save(table: String, contentValues: ContentValues): Long {
        Log.e("DATABASE", "Insert in table $table: ${contentValues.valueSet()}")
        return insertOrThrow(table, contentValues)
    }
    
}
