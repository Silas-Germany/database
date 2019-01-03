package com.github.silasgermany.complexorm

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.github.silasgermany.complexormapi.GeneratedSqlTablesInterface
import com.github.silasgermany.complexormapi.SqlAllTables
import com.github.silasgermany.complexormapi.SqlTable
import com.github.silasgermany.complexormapi.SqlTypes
import org.threeten.bp.LocalDate
import java.io.File
import java.util.*
import kotlin.reflect.KClass

@SqlAllTables
object SqlWriter: SqlUtils {

    private val sqlTables =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlTables").getDeclaredField("INSTANCE").get(null) as GeneratedSqlTablesInterface

    private fun getIdentifier(tableClass: KClass<*>) = tableClass.java.name
        .run { substring(lastIndexOf('.') + 1).split('$') }.let { it[0] to it[1].sql }

    private fun getNormalColumns(identifier: Pair<String, String>) = identifier.run { sqlTables.normalColumns[first]?.get(second) }
    private fun joinColumns(identifier: Pair<String, String>) = identifier.run { sqlTables.joinColumns[first]?.get(second) }
    private fun connectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.connectedColumns[first]?.get(second) }
    private fun reverseConnectedColumn(identifier: Pair<String, String>) = identifier.run { sqlTables.reverseConnectedColumns[first]?.get(second) }

    fun write(table: SqlTable): String {
        SQLiteDatabase.openOrCreateDatabase(File("/tmp/database.db"), null).run {
            beginTransaction()
            try {
                return write(table).also { setTransactionSuccessful() }
            } finally {
                endTransaction()
            }
        }
    }

    private fun SQLiteDatabase.write(table: SqlTable): String {
        val contentValues = ContentValues()
        table.transferId()
        val identifier = getIdentifier(table::class)
        val normalColumns = getNormalColumns(identifier)
        val joinColumns = joinColumns(identifier)
        val connectedColumn = connectedColumn(identifier)
        val reverseConnectedColumn = reverseConnectedColumn(identifier)
        var additionalInserts = ""
        table.map.forEach { (key, value) ->
            val sqlKey = key.sql
            normalColumns?.get(sqlKey)?.also {
                if (value == null) contentValues.putNull(key)
                else when (it) {
                    SqlTypes.String -> contentValues.put(key, value as String)
                    SqlTypes.Int -> contentValues.put(key, value as Int)
                    SqlTypes.Boolean -> contentValues.put(key, value as Boolean)
                    SqlTypes.Long -> contentValues.put(key, value as Long)
                    SqlTypes.Date -> contentValues.put(key, (value as Date).time)
                    SqlTypes.LocalDate -> contentValues.put(key, (value as LocalDate).toEpochDay())
                    SqlTypes.ByteArray -> contentValues.put(key, value as ByteArray)
                    SqlTypes.SqlTable,
                    SqlTypes.SqlTables -> {
                        throw IllegalArgumentException("Normal table shouldn't have SqlTable inside")
                    }
                }
            }
            joinColumns?.get(sqlKey)?.let { joinTable ->
                try {
                    delete("${identifier.second}_$sqlKey", "${identifier.second}_id = ${table.id}", null)
                    val innerContentValues = ContentValues()
                    innerContentValues.put("${identifier.second}_id", table.id)
                    (value as List<*>).forEach { joinTableEntry ->
                        innerContentValues.put("${joinTable}_id", (joinTableEntry as SqlTable).id)
                        insertOrThrow("${identifier.second}_$sqlKey", null, innerContentValues)
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
                        contentValues.put("${key}_id", connectedEntry.id.toString())
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save connected table entry: $value")
                }
            }
            reverseConnectedColumn?.get(sqlKey)?.let { joinTableData ->
                try {
                    val innerContentValues = ContentValues()
                    (value as List<*>).forEach { joinTableEntry ->
                        innerContentValues.put("${joinTableData.second ?: identifier.second}_id", table.id)
                        update(joinTableData.first, innerContentValues, "_id = ${(joinTableEntry as SqlTable).id}", null)
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save reverse connected table entries: $value")
                }
            }
        }
        table.map["_id"] = insertOrThrow(identifier.first, null, contentValues).toInt()
        return table.toString()
    }
}
