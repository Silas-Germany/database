package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabase
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import com.github.silasgermany.complexormapi.IdType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ComplexOrmWriter internal constructor(val database: ComplexOrmDatabase,
                                            private val complexOrmTableInfo: ComplexOrmTableInfoInterface) {

    private fun String.toSql() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
    fun execSQL(sql: String) = database.execSQL(sql)

    fun save(table: ComplexOrmTable, writeDeep: Boolean = true): Boolean {
        return database.doInTransaction {
            write(table, writeDeep)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val ComplexOrmTable.tableName get() = complexOrmTableInfo.basicTableInfo
        .getValue(longName).first
    @Suppress("MemberVisibilityCanBePrivate")
    val KClass<out ComplexOrmTable>.tableName get() = complexOrmTableInfo.basicTableInfo
        .getValue(longName).first

    private fun write(table: ComplexOrmTable, writeDeep: Boolean = true): Boolean {
        val contentValues = mutableMapOf<String, Any?>()
        val tableName = table.tableName
        val tableClassName: String = table.longName
        val rootTableClass = complexOrmTableInfo.basicTableInfo.
            getValue(table.longName).second
        val normalColumns = (complexOrmTableInfo.normalColumns[rootTableClass] ?: mapOf()) +
                (complexOrmTableInfo.normalColumns[tableClassName] ?: mapOf()) +
                mapOf("id" to ComplexOrmTypes.IdType.name)
        val joinColumns = (complexOrmTableInfo.joinColumns[rootTableClass] ?: mapOf()) +
                (complexOrmTableInfo.joinColumns[tableClassName] ?: mapOf())
        val reverseJoinColumns = (complexOrmTableInfo.reverseJoinColumns[rootTableClass] ?: mapOf()) +
                (complexOrmTableInfo.reverseJoinColumns[tableClassName] ?: mapOf())
        val connectedColumns = (complexOrmTableInfo.connectedColumns[rootTableClass] ?: mapOf()) +
                (complexOrmTableInfo.connectedColumns[tableClassName] ?: mapOf())
        val specialConnectedColumns = (complexOrmTableInfo.specialConnectedColumns[rootTableClass] ?: mapOf()) +
                (complexOrmTableInfo.specialConnectedColumns[tableClassName] ?: mapOf())
        val reverseConnectedColumns = (complexOrmTableInfo.reverseConnectedColumns[rootTableClass] ?: mapOf()) +
                (complexOrmTableInfo.reverseConnectedColumns[tableClassName] ?: mapOf())
        table.map.forEach { (key, value) ->
            val sqlKey = key.toSql()
            var keyFound = false
            normalColumns[sqlKey]?.also { type ->
                if (type !in ComplexOrmTypes.values().map { it.name })
                    throw IllegalArgumentException("Can't save value $value (unknown type: $type)")
                keyFound = true
                if (value != null || sqlKey != "id") {
                    contentValues[sqlKey] = value
                }
            }
            connectedColumns[sqlKey]?.let {
                keyFound = true
                try {
                    val connectedEntry = (value as ComplexOrmTable?)
                    if (connectedEntry?.run { id == null } == true) {
                        if (!writeDeep) return@let
                        write(connectedEntry, writeDeep)
                    }
                    contentValues["${sqlKey.toSql()}_id"] = connectedEntry?.id
                } catch (e: Throwable) {
                    throw IllegalArgumentException("Couldn't save connected table entry: $value (${e.message})", e)
                }
            }
            specialConnectedColumns[sqlKey]?.let {
                throw UnsupportedOperationException("Can't save (yet) entries, that have special connected columns")
            }
            when (sqlKey) {
                in joinColumns,
                in reverseJoinColumns,
                in reverseConnectedColumns -> keyFound = true
            }
            if (!keyFound) throw IllegalArgumentException("Couldn't find column $sqlKey in $tableClassName")
        }
        try {
            save(tableName, contentValues)?.let { table.map["id"] = it }
        } catch (e: Throwable) {
            throw IllegalArgumentException("Couldn't save table entries: $table (${e.message})", e)
        }
        table.map.forEach { (key, value) ->
            val sqlKey = key.toSql()
            joinColumns[sqlKey]?.let { joinTable ->
                try {
                    val joinTableName = complexOrmTableInfo.basicTableInfo.getValue(joinTable).first
                    delete("${tableName}_$sqlKey", "${tableName}_id", table.id)
                    val innerContentValues = mutableMapOf<String, Any?>()
                    innerContentValues["${tableName}_id"] = table.id
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) {
                            if (!writeDeep) return@let
                            write(joinTableEntry, writeDeep)
                        }
                        innerContentValues["${joinTableName}_id"] = joinTableEntry.id
                        save("${tableName}_$sqlKey", innerContentValues)
                    }
                } catch (e: Throwable) {
                    throw IllegalArgumentException("Couldn't save joined table entries: $value (${e.message})", e)
                }
            }
            reverseJoinColumns[sqlKey]?.let { reverseJoinTableData ->
                try {
                    val (reverseJoinTableDataFirst, reverseJoinTableDataSecond) = reverseJoinTableData.split(';').let { it[0] to it[1] }
                    val joinTableName = complexOrmTableInfo.basicTableInfo.getValue(reverseJoinTableDataFirst).first
                    delete("${joinTableName}_$reverseJoinTableDataSecond", "${tableName}_id", table.id)
                    val innerContentValues = mutableMapOf<String, Any?>()
                    innerContentValues["${tableName}_id"] = table.id
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) {
                            if (!writeDeep) return@let
                            write(joinTableEntry, writeDeep)
                        }
                        innerContentValues["${joinTableName}_id"] = joinTableEntry.id
                        save("${joinTableName}_$reverseJoinTableDataSecond", innerContentValues)
                    }
                } catch (e: Throwable) {
                    throw IllegalArgumentException("Couldn't save joined table entries: $value (${e.message})", e)
                }
            }
            reverseConnectedColumns[sqlKey]?.let { reverseConnectedTableData ->
                try {
                    val (reverseConnectedTableDataFirst, reverseConnectedTableDataSecond) = reverseConnectedTableData.split(';').let { it[0] to it[1] }
                    val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(reverseConnectedTableDataFirst).first
                    val innerContentValues = mutableMapOf<String, Any?>()
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) {
                            if (!writeDeep) return@let
                            write(joinTableEntry, writeDeep)
                        }
                        innerContentValues["${reverseConnectedTableDataSecond}_id"] = table.id
                        database.updateOne(connectedTableName, innerContentValues, joinTableEntry.id)
                    }
                } catch (e: Throwable) {
                    throw IllegalArgumentException("Couldn't save reverse connected table entries: $value (${e.message})", e)
                }
            }
        }
        return table.id != null
    }

    operator fun <K, V> Map<out K, V>?.contains(key: K) = this?.containsKey(key) == true

    private fun delete(table: String, column: String, id: IdType?) {
        id ?: return
        database.delete(table, "$column=$id")
    }

    private fun save(table: String, contentValues: MutableMap<String, Any?>): IdType? {
        return database.insertOrUpdate(table, contentValues)
    }

    fun <T: ComplexOrmTable, R> saveOneColumn(table: KClass<T>, column: KProperty1<T, R?>, id: IdType, value: R) {
        val contentValues = mutableMapOf<String, Any?>()
        var columnName = column.name.toSql()
        if (tableInfo.normalColumns[table.longName]?.get(columnName) == null) {
            columnName += "_id"
        }
        contentValues[columnName] = value
        contentValues["id"] = id
        save(table.tableName, contentValues)
    }

    fun <T: ComplexOrmTable> changeId(table: KClass<T>, oldId: IdType, newId: IdType) {
        if (oldId == newId) return
        database.doInTransaction {
            val tableClass = table.longName
            val tableName = table.tableName
            val values = mutableMapOf<String, Any?>()
            values[ComplexOrmTable::id.name.toSql()] = newId
            try {
                if (!database.updateOne(tableName, values, oldId)) return@doInTransaction
            } catch (e: Throwable) {
                if (!e.toString().contains("SQLiteConstraintException")) throw e
                database.deleteOne(tableName, newId)
                if (!database.updateOne(tableName, values, oldId)) return@doInTransaction
            }
            complexOrmTableInfo.connectedColumns.forEach { connectedTable ->
                connectedTable.value.filter { it.value == tableClass }.forEach { connectedColumn ->
                    val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(connectedTable.key).first
                    val idColumnName = "${connectedColumn.key}_id"
                    val contentValues = mutableMapOf<String, Any?>()
                    contentValues[idColumnName] = newId
                    database.update(connectedTableName, contentValues, "$idColumnName = ${oldId.asSql}")
                }
            }
            complexOrmTableInfo.joinColumns.forEach { connectedTable ->
                connectedTable.value.filter { it.value == tableClass }.forEach { connectedColumn ->
                    val idColumnName = "${tableName}_id"
                    val contentValues = mutableMapOf<String, Any?>()
                    contentValues[idColumnName] = newId
                    database.update("${tableName}_${connectedColumn.key}", contentValues,
                        "$idColumnName = ${oldId.asSql}")
                }
            }
            complexOrmTableInfo.reverseJoinColumns.forEach { connectedTable ->
                connectedTable.value.filter { it.value.split(';')[0] == tableClass }.forEach { connectedTableAndColumn ->
                    val (connectedTableClass, connectedColumn) = connectedTableAndColumn.value.split(';')
                    val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(connectedTableClass).first
                    val idColumnName = "${tableName}_id"
                    val contentValues = mutableMapOf<String, Any?>()
                    contentValues[idColumnName] = newId
                    database.update("${connectedTableName}_$connectedColumn", contentValues,
                        "$idColumnName = ${oldId.asSql}")
                }
            }
            complexOrmTableInfo.reverseConnectedColumns.forEach { connectedTable ->
                connectedTable.value.filter { it.value.split(';')[0] == tableClass }.forEach { connectedTableAndColumn ->
                    val (connectedTableClass, connectedColumn) = connectedTableAndColumn.value.split(';')
                    val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(connectedTableClass).first
                    val idColumnName = "${connectedColumn}_id"
                    val contentValues = mutableMapOf<String, Any?>()
                    contentValues[idColumnName] = newId
                    database.update(connectedTableName, contentValues,
                        "$idColumnName = ${oldId.asSql}")
                }
            }
        }
    }

    inline fun <T>doInTransaction(f: () -> T) = database.doInTransaction(f)
}
