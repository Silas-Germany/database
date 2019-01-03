package com.github.silasgermany.complexorm

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.github.silasgermany.complexormapi.*
import org.threeten.bp.LocalDate
import java.io.File
import java.util.*
import kotlin.reflect.KClass

@SqlAllTables
object SqlWriter: SqlUtils {

    private val sqlSchema =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlSchema").getDeclaredField("INSTANCE").get(null) as GeneratedSqlSchemaInterface
    private val sqlTables =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlTables").getDeclaredField("INSTANCE").get(null) as GeneratedSqlTablesInterface

    private fun getIdentifier(tableClass: KClass<*>) = tableClass.java.name
        .run { substring(lastIndexOf('.') + 1).split('$') }.let { it[0] to it[1].sql }

    private fun getNormalColumns(identifier: Pair<String, String>) = identifier.run { sqlTables.normalColumns[first]?.get(second) }
    private fun joinColumns(identifier: Pair<String, String>) = identifier.run { sqlTables.joinColumns[first]?.get(second) }
    private fun connectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.connectedColumns[first]?.get(second) }
    private fun reverseConnectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.reverseConnectedColumns[first]?.get(second) }

    fun write(table: SqlTable, path: String): String {
        val databaseFile = File(path)//File.createTempFile("database", ".db")
        Log.e("DATABASE", "${databaseFile.path}")
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).run {
            beginTransactionNonExclusive()
            try {
                sqlSchema.dropTableCommands.forEach { execSQL(it) }
                sqlSchema.createTableCommands.forEach { execSQL(it) }
                return write(table).also { setTransactionSuccessful() }
            } finally {
                endTransaction()
            }
        }
    }

    private fun SQLiteDatabase.write(table: SqlTable): String {
        Log.e("DATABASE", "Save $table")
        val contentValues = ContentValues()
        table.transferId()
        val identifier = getIdentifier(table::class)
        val normalColumns = getNormalColumns(identifier)
        val joinColumns = joinColumns(identifier)
        val connectedColumn = connectedColumn(identifier)
        val reverseConnectedColumn = reverseConnectedColumn(identifier)
        var additionalInserts = mapOf<String, ContentValues>()
        table.map.forEach { (key, value) ->
            val sqlKey = key.sql
            normalColumns?.get(sqlKey)?.also {
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
            if (value is SqlTable) {
                try {
                    val connectedEntry = (value as SqlTable?)
                    if (connectedEntry != null) {
                        if (connectedEntry.id == null) write(connectedEntry)
                        contentValues.put("${key.sql}_id", connectedEntry.id.toString())
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save connected table entry: $value (${e.message})")
                }
            }
        }
        try {
            Log.e("DATABASE", "Insert in ${identifier.second}: ${contentValues.valueSet()}")
            table.map["_id"] = insertOrThrow(identifier.second, null, contentValues)
        } catch (e: Exception) {
            throw IllegalArgumentException("Couldn't save reverse connected table entries: $table (${e.message})")
        }
        table.map.forEach { (key, value) ->
            val sqlKey = key.sql
            joinColumns?.get(sqlKey)?.let { joinTable ->
                try {
                    delete("${identifier.second}_$sqlKey", "${identifier.second}_id = ${table.id}", null)
                    val innerContentValues = ContentValues()
                    innerContentValues.put("${identifier.second}_id", table.id)
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as SqlTable
                        if (joinTableEntry.id == null) write(joinTableEntry)
                        innerContentValues.put("${joinTable}_id", (joinTableEntry as SqlTable).id)
                        Log.e("DATABASE", "Insert in ${identifier.second}_$sqlKey: ${innerContentValues.valueSet()}")
                        insertOrThrow("${identifier.second}_$sqlKey", null, innerContentValues)
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
                        if (joinTableEntry.id == null) {
                            joinTableEntry.map[joinTableData.second ?: identifier.second] = table
                            Log.e("DATABASE", "Save $table")
                            write(joinTableEntry)
                        } else {
                            innerContentValues.put("${joinTableData.second ?: identifier.second}_id", table.id)
                            Log.e("DATABASE", "Insert in ${joinTableData.first}: ${innerContentValues.valueSet()}")
                            update(joinTableData.first, innerContentValues, "_id = ${joinTableEntry.id}", null)
                        }
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save reverse connected table entries: $value (${e.message})")
                }
            }
        }
        return table.toString()
    }
}
