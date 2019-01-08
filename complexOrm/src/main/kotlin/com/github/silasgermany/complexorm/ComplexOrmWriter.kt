package com.github.silasgermany.complexorm

import android.content.ContentValues
import android.util.Log
import com.github.silasgermany.complexormapi.ComplexOrmSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTablesInterface
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import org.threeten.bp.LocalDate
import java.io.File
import java.util.*
import kotlin.reflect.KClass

class ComplexOrmWriter(private val database: ComplexOrmDatabase): ComplexOrmUtils() {

    constructor(databaseFile: File) : this(ComplexOrmDatabase(databaseFile))

    private val sqlSchema =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlSchema")
            .getDeclaredField("INSTANCE").get(null) as ComplexOrmSchemaInterface
    private val sqlTables =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlTables")
            .getDeclaredField("INSTANCE").get(null) as ComplexOrmTablesInterface

    private fun getIdentifier(tableClass: KClass<*>) = tableClass.java.name
        .run { substring(lastIndexOf('.') + 1).split('$') }.let { it[0] to it[1].sql }

    private fun getNormalColumns(identifier: Pair<String, String>) = mapOf("_id" to ComplexOrmTypes.Long).plus(identifier.run { sqlTables.normalColumns[first]?.get(second) } ?: emptyMap())
    private fun joinColumns(identifier: Pair<String, String>) = identifier.run { sqlTables.joinColumns[first]?.get(second) }
    private fun connectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.connectedColumns[first]?.get(second) }
    private fun reverseConnectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.reverseConnectedColumns[first]?.get(second) }

    fun write(table: ComplexOrmTable): String {
        return database.use {
                sqlSchema.dropTableCommands.forEach { rawSql(it) }
                sqlSchema.createTableCommands.forEach { rawSql(it) }
            write2(table)
        }
    }

    private fun ComplexOrmDatabase.write2(table: ComplexOrmTable): String {
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
                    ComplexOrmTypes.String -> contentValues.put(key, value as String)
                    ComplexOrmTypes.Int -> contentValues.put(key, value as Int)
                    ComplexOrmTypes.Boolean -> contentValues.put(key, value as Boolean)
                    ComplexOrmTypes.Long -> contentValues.put(key, value as Long)
                    ComplexOrmTypes.Float -> contentValues.put(key, value as Float)
                    ComplexOrmTypes.Date -> contentValues.put(key, (value as Date).time)
                    ComplexOrmTypes.LocalDate -> contentValues.put(key, (value as LocalDate).toEpochDay())
                    ComplexOrmTypes.ByteArray -> contentValues.put(key, value as ByteArray)
                    ComplexOrmTypes.ComplexOrmTable,
                    ComplexOrmTypes.ComplexOrmTables -> {
                        throw IllegalArgumentException("Normal table shouldn't have ComplexOrmTable inside")
                    }
                }.let { } // this is checking, that the when is exhaustive
            }
            connectedColumn?.get(sqlKey)?.let {
                try {
                    val connectedEntry = (value as ComplexOrmTable?)
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
                        joinTableEntry as ComplexOrmTable
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
                        joinTableEntry as ComplexOrmTable
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

    private fun ComplexOrmDatabase.save(table: String, contentValues: ContentValues): Long {
        Log.e("DATABASE", "Insert in table $table: ${contentValues.valueSet()}")
        return insertOrThrow(table, contentValues)
    }
    
}
