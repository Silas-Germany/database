package com.github.silasgermany.complexorm

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ComplexOrmWriter internal constructor(private val database: ComplexOrmDatabaseInterface,
                       private val complexOrmTableInfo: ComplexOrmTableInfoInterface) {

    private fun String.toSql() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()

    fun save(table: ComplexOrmTable, writeDeep: Boolean = true): Boolean {
        try {
            database.beginTransaction()
            return write(table, writeDeep)
                    .apply { database.setTransactionSuccessful() }
        } finally {
            database.endTransaction()
        }
    }

    val ComplexOrmTable.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(javaClass.canonicalName!!).first
    val KClass<out ComplexOrmTable>.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(java.canonicalName!!).first

    private fun write(table: ComplexOrmTable, writeDeep: Boolean = true): Boolean {
        val contentValues = ContentValues()
        val tableName = table.tableName
        val rootTableClass = complexOrmTableInfo.basicTableInfo.getValue(table.javaClass.canonicalName!!).second
        val normalColumns = (complexOrmTableInfo.normalColumns[rootTableClass] ?: sortedMapOf())+
                mapOf("id" to "Int")
        val joinColumns = complexOrmTableInfo.joinColumns[rootTableClass]
        val reverseJoinColumns = complexOrmTableInfo.reverseJoinColumns[rootTableClass]
        val connectedColumn = complexOrmTableInfo.connectedColumns[rootTableClass]
        val specialConnectedColumn = complexOrmTableInfo.specialConnectedColumns[rootTableClass]
        val reverseConnectedColumn = complexOrmTableInfo.reverseConnectedColumns[rootTableClass]
        table.map.forEach { (key, value) ->
            val sqlKey = key.toSql()
            var keyFound = false
            normalColumns[sqlKey]?.also { type ->
                keyFound = true
                if (value == null) contentValues.putNull(sqlKey)
                else when (ComplexOrmTypes.values().find { it.name == type } ?: throw java.lang.IllegalStateException("NOT A TYPE: $type (${ComplexOrmTypes.values().map { it.name }}")) {
                    ComplexOrmTypes.String -> contentValues.put(sqlKey, value as String)
                    ComplexOrmTypes.Int -> contentValues.put(sqlKey, value as Int)
                    ComplexOrmTypes.Boolean -> contentValues.put(sqlKey, if (value as Boolean) 1 else 0)
                    ComplexOrmTypes.Long -> contentValues.put(sqlKey, value as Long)
                    ComplexOrmTypes.Float -> contentValues.put(sqlKey, value as Float)
                    ComplexOrmTypes.Date -> contentValues.put(sqlKey, (value as Date).time)
                    ComplexOrmTypes.LocalDate -> contentValues.put(sqlKey, (value as LocalDate).toEpochDay())
                    ComplexOrmTypes.ByteArray -> contentValues.put(sqlKey, value as ByteArray)
                    else -> {
                        throw IllegalStateException("Normal table shouldn't have ComplexOrmTable inside")
                    }
                }
            }
            connectedColumn?.get(sqlKey)?.let {
                keyFound = true
                try {
                    val connectedEntry = (value as ComplexOrmTable?)
                    if (connectedEntry != null) {
                        if (connectedEntry.id == null) {
                            if (!writeDeep) return@let
                            write(connectedEntry)
                        }
                        contentValues.put("${sqlKey.toSql()}_id", connectedEntry.id.toString())
                    } else contentValues.putNull("${sqlKey.toSql()}_id")
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save connected table entry: $value (${e.message})", e)
                }
            }
            specialConnectedColumn?.get(sqlKey)?.let {
                throw UnsupportedOperationException("Can't save (yet) entries, that have special connected columns")
            }
            if (!keyFound) throw IllegalArgumentException("Couldn't find column $sqlKey in $rootTableClass")
        }
        try {
            table.map["id"] = save(tableName, contentValues)
        } catch (e: Exception) {
            throw IllegalArgumentException("Couldn't save table entries: $table (${e.message})", e)
        }
        table.map.forEach { (key, value) ->
            val sqlKey = key.toSql()
            joinColumns?.get(sqlKey)?.let { joinTable ->
                try {
                    val joinTableName = complexOrmTableInfo.basicTableInfo.getValue(joinTable).first
                    delete("${tableName}_$sqlKey", "${tableName}_id", table.id)
                    val innerContentValues = ContentValues()
                    innerContentValues.put("${tableName}_id", table.id)
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) {
                            if (!writeDeep) return@let
                            write(joinTableEntry)
                        }
                        innerContentValues.put("${joinTableName}_id", joinTableEntry.id)
                        save("${tableName}_$sqlKey", innerContentValues)
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save joined table entries: $value (${e.message})", e)
                }
            }
            reverseJoinColumns?.get(sqlKey)?.let { reverseJoinTableData ->
                try {
                    val (reverseJoinTableDataFirst, reverseJoinTableDataSecond) = reverseJoinTableData.split(';').let { it[0] to it[1] }
                    val joinTableName = complexOrmTableInfo.basicTableInfo.getValue(reverseJoinTableDataFirst).first
                    val columnName = reverseJoinTableDataSecond
                    delete("${joinTableName}_$columnName", "${tableName}_id", table.id)
                    val innerContentValues = ContentValues()
                    innerContentValues.put("${tableName}_id", table.id)
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) {
                            if (!writeDeep) return@let
                            write(joinTableEntry)
                        }
                        innerContentValues.put("${joinTableName}_id", joinTableEntry.id)
                        save("${joinTableName}_$columnName", innerContentValues)
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save joined table entries: $value (${e.message})", e)
                }
            }
            reverseConnectedColumn?.get(sqlKey)?.let { reverseConnectedTableData ->
                try {
                    val (reverseConnectedTableDataFirst, reverseConnectedTableDataSecond) = reverseConnectedTableData.split(';').let { it[0] to it[1] }
                    val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(reverseConnectedTableDataFirst).first
                    val innerContentValues = ContentValues()
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) {
                            if (!writeDeep) return@let
                            write(joinTableEntry)
                        }
                        innerContentValues.put("${reverseConnectedTableDataSecond}_id", table.id)
                        update(connectedTableName, innerContentValues, joinTableEntry.id)
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save reverse connected table entries: $value (${e.message})", e)
                }
            }
            Unit
        }
        return table.id ?: 0 > 0
    }

    private fun delete(table: String, column: String, value: Int?) {
        value ?: return
        database.delete(table, "$column = $value", null)
    }

    private fun save(table: String, contentValues: ContentValues): Int {
        var changedId = database.insertWithOnConflict(table, "id", contentValues, SQLiteDatabase.CONFLICT_IGNORE).toInt()
        if (changedId == -1) {
            changedId = contentValues.getAsInteger("id") ?: throw java.lang.IllegalArgumentException("Couldn't insert values $contentValues in $table")
            val changed = database.updateWithOnConflict(table, contentValues, "id = $changedId", null, SQLiteDatabase.CONFLICT_ROLLBACK)
            if (changed != 1) throw java.lang.IllegalArgumentException("Couldn't update values $contentValues for $table")
        }
        return changedId
    }

    private fun update(table: String, contentValues: ContentValues, id: Int?) {
        id ?: return
        val changed = database.updateWithOnConflict(table, contentValues, "id = $id", null, SQLiteDatabase.CONFLICT_ROLLBACK)
        if (changed != 1) throw java.lang.IllegalArgumentException("Couldn't update values $contentValues for $table")
    }

    fun <T: ComplexOrmTable, R> saveOneColumn(table: KClass<T>, column: KProperty1<T, R?>, id: Int, value: R) {
        val contentValues = ContentValues()
        when (value) {
            is Int -> contentValues.put(column.name.toSql(), value)
            is String -> contentValues.put(column.name.toSql(), value)
            is Long -> contentValues.put(column.name.toSql(), value)
            is Boolean -> contentValues.put(column.name.toSql(), value)
            is ByteArray -> contentValues.put(column.name.toSql(), value)
            else -> throw IllegalArgumentException("Can't save value $value (unknown type)")
        }
        contentValues.put("id", id)
        save(table.tableName, contentValues)
    }

    fun <T: ComplexOrmTable> changeId(table: KClass<T>, oldId: Int, newId: Int, additionalValues: ContentValues? = null) {
        val tableClass = table.java.canonicalName
        val tableName = table.tableName
        (additionalValues ?: ContentValues()).also {
            if (oldId != newId) it.put(ComplexOrmTable::id.name.toSql(), newId)
            try {
                database.updateWithOnConflict(tableName, it, "id = $oldId", null, SQLiteDatabase.CONFLICT_ROLLBACK)
            } catch (e: Exception) {
                if (!e.toString().contains("SQLiteConstraintException")) throw e
                database.delete(tableName, "id = $newId", null)
                database.updateWithOnConflict(tableName, it, "id = $oldId", null, SQLiteDatabase.CONFLICT_ROLLBACK)
            }
        }
        if (oldId == newId) return
        complexOrmTableInfo.connectedColumns.forEach { connectedTable ->
            connectedTable.value.filter { it.value == tableClass }.forEach { connectedColumn ->
                val idColumnName = "${tableName}_id"
                val contentValues = ContentValues()
                contentValues.put(idColumnName, newId)
                database.updateWithOnConflict("${tableName}_${connectedColumn.key}", contentValues,
                        "$idColumnName = $oldId", null, SQLiteDatabase.CONFLICT_ROLLBACK)
            }
        }
        complexOrmTableInfo.joinColumns.forEach { connectedTable ->
            connectedTable.value.filter { it.value == tableClass }.forEach { connectedColumn ->
                val idColumnName = "${tableName}_id"
                val contentValues = ContentValues()
                contentValues.put(idColumnName, newId)
                database.updateWithOnConflict("${tableName}_${connectedColumn.key}", contentValues,
                        "$idColumnName = $oldId", null, SQLiteDatabase.CONFLICT_ROLLBACK)
            }
        }
        complexOrmTableInfo.reverseJoinColumns.forEach { connectedTable ->
            connectedTable.value.filter { it.value.split(';')[0] == tableClass }.forEach { connectedTableAndColumn ->
                val (connectedTableClass, connectedColumn) = connectedTableAndColumn.value.split(';')
                val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(connectedTableClass).first
                val idColumnName = "${tableName}_id"
                val contentValues = ContentValues()
                contentValues.put(idColumnName, newId)
                database.updateWithOnConflict("${connectedTableName}_$connectedColumn", contentValues,
                        "$idColumnName = $oldId", null, SQLiteDatabase.CONFLICT_ROLLBACK)
            }
        }
        complexOrmTableInfo.reverseJoinColumns.forEach { connectedTable ->
            connectedTable.value.filter { it.value.split(';')[0] == tableClass }.forEach { connectedTableAndColumn ->
                val (connectedTableClass, connectedColumn) = connectedTableAndColumn.value.split(';')
                val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(connectedTableClass).first
                val idColumnName = "${connectedColumn}_id"
                val contentValues = ContentValues()
                contentValues.put(idColumnName, newId)
                database.updateWithOnConflict(connectedTableName, contentValues,
                        "$idColumnName = $oldId", null, SQLiteDatabase.CONFLICT_ROLLBACK)
            }
        }
    }
}