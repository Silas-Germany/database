package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabase
import com.github.silasgermany.complexormapi.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ComplexOrmWriter internal constructor(val database: ComplexOrmDatabase,
                                            private val complexOrmTableInfo: ComplexOrmTableInfoInterface) {

    private fun String.toSql() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
    private val IdType?.asSql get() = this?.let { _ ->
        "x'${toString().replace("-", "")}'"
    }
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
                keyFound = true
                if (value == null) {
                    if (sqlKey != "id") contentValues[sqlKey] = null
                } else when (ComplexOrmTypes.values().find { it.name == type }
                    ?: throw IllegalStateException("NOT A TYPE: $type (${ComplexOrmTypes.values().map { it.name }}")) {
                    ComplexOrmTypes.IdType -> contentValues[sqlKey] = (value as IdType)
                    ComplexOrmTypes.Boolean -> contentValues[sqlKey] = if (value as Boolean) 1 else 0
                    ComplexOrmTypes.Int -> contentValues[sqlKey] = value as Int
                    ComplexOrmTypes.Long -> contentValues[sqlKey] = value as Long
                    ComplexOrmTypes.Float -> contentValues[sqlKey] = value as Float
                    ComplexOrmTypes.String -> contentValues[sqlKey] = value as String
                    ComplexOrmTypes.Date -> contentValues[sqlKey] = (value as Date).asSql
                    ComplexOrmTypes.DateTime -> contentValues[sqlKey] = ((value as CommonDateTime).getMillis() / 1000).toInt()
                    ComplexOrmTypes.ByteArray -> contentValues[sqlKey] = value as ByteArray
                }
            }
            connectedColumns[sqlKey]?.let {
                keyFound = true
                try {
                    val connectedEntry = (value as ComplexOrmTable?)
                    if (connectedEntry != null) {
                        if (connectedEntry.id == null) {
                            if (!writeDeep) return@let
                            write(connectedEntry)
                        }
                        contentValues["${sqlKey.toSql()}_id"] = connectedEntry.id.toString()
                    } else contentValues["${sqlKey.toSql()}_id"] = null
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
                            write(joinTableEntry)
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
                            write(joinTableEntry)
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
                            write(joinTableEntry)
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
        when (value) {
            is Int -> contentValues[column.name.toSql()] = value
            is String -> contentValues[column.name.toSql()] = value
            is Long -> contentValues[column.name.toSql()] = value
            is Boolean -> contentValues[column.name.toSql()] = value
            is ByteArray -> contentValues[column.name.toSql()] = value
            else -> throw IllegalArgumentException("Can't save value $value (unknown type)")
        }
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
