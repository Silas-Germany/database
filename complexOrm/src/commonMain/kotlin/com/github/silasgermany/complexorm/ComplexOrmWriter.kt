package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexormapi.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ComplexOrmWriter internal constructor(private val database: ComplexOrmDatabaseInterface,
                                            private val complexOrmTableInfo: ComplexOrmTableInfoInterface) {

    private fun String.toSql() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
    private val CommonUUID?.asSql get() = this?.let { _ ->
        "x'${toString().replace("-", "")}'"
    }
    fun execSQL(sql: String) = database.execSQL(sql)

    fun save(table: ComplexOrmTable, writeDeep: Boolean = true): Boolean {
        try {
            database.beginTransaction()
            return write(table, writeDeep)
                .apply { database.setTransactionSuccessful() }
        } finally {
            database.endTransaction()
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val ComplexOrmTable.tableName get() = complexOrmTableInfo.basicTableInfo
        .getValue(longName).first
    @Suppress("MemberVisibilityCanBePrivate")
    val KClass<out ComplexOrmTable>.tableName get() = complexOrmTableInfo.basicTableInfo
        .getValue(longName).first

    private fun write(table: ComplexOrmTable, writeDeep: Boolean = true): Boolean {
        val contentValues = CommonContentValues()
        val tableName = table.tableName
        val tableClassName: String = table.longName
        val rootTableClass = complexOrmTableInfo.basicTableInfo.
            getValue(table.longName).second
        val normalColumns = (complexOrmTableInfo.normalColumns[rootTableClass] ?: mapOf()) +
                (complexOrmTableInfo.normalColumns[tableClassName] ?: mapOf()) +
                mapOf("id" to ComplexOrmTypes.Uuid.name)
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
                    if (sqlKey != "id") contentValues.putNull(sqlKey)
                } else when (ComplexOrmTypes.values().find { it.name == type }
                    ?: throw IllegalStateException("NOT A TYPE: $type (${ComplexOrmTypes.values().map { it.name }}")) {
                    ComplexOrmTypes.String -> contentValues.put(sqlKey, value as String)
                    ComplexOrmTypes.Int -> contentValues.put(sqlKey, value as Int)
                    ComplexOrmTypes.Boolean -> contentValues.put(sqlKey, if (value as Boolean) 1 else 0)
                    ComplexOrmTypes.Long -> contentValues.put(sqlKey, value as Long)
                    ComplexOrmTypes.Float -> contentValues.put(sqlKey, value as Float)
                    ComplexOrmTypes.Date -> contentValues.put(sqlKey, (value as Day).asSql)
                    ComplexOrmTypes.DateTime -> contentValues.put(sqlKey, ((value as CommonDateTime).getMillis() / 1000).toInt())
                    ComplexOrmTypes.Uuid -> contentValues.put(sqlKey, (value as CommonUUID).asByteArray)
                    ComplexOrmTypes.ByteArray -> contentValues.put(sqlKey, value as ByteArray)
                    else -> {
                        throw IllegalStateException("Normal table shouldn't have ComplexOrmTable inside")
                    }
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
                        contentValues.put("${sqlKey.toSql()}_id", connectedEntry.id.toString())
                    } else contentValues.putNull("${sqlKey.toSql()}_id")
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
            throw e//IllegalArgumentException("Couldn't save table entries: $table (${e.message})", e)
        }
        table.map.forEach { (key, value) ->
            val sqlKey = key.toSql()
            joinColumns[sqlKey]?.let { joinTable ->
                try {
                    val joinTableName = complexOrmTableInfo.basicTableInfo.getValue(joinTable).first
                    delete("${tableName}_$sqlKey", "${tableName}_id", table.id)
                    val innerContentValues = CommonContentValues()
                    innerContentValues.put("${tableName}_id", table.id.asByteArray)
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) {
                            if (!writeDeep) return@let
                            write(joinTableEntry)
                        }
                        innerContentValues.put("${joinTableName}_id", joinTableEntry.id.asByteArray)
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
                    val innerContentValues = CommonContentValues()
                    innerContentValues.put("${tableName}_id", table.id.asByteArray)
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) {
                            if (!writeDeep) return@let
                            write(joinTableEntry)
                        }
                        innerContentValues.put("${joinTableName}_id", joinTableEntry.id.asByteArray)
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
                    val innerContentValues = CommonContentValues()
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) {
                            if (!writeDeep) return@let
                            write(joinTableEntry)
                        }
                        innerContentValues.put("${reverseConnectedTableDataSecond}_id", table.id.asByteArray)
                        update(connectedTableName, innerContentValues, joinTableEntry.id)
                    }
                } catch (e: Throwable) {
                    throw IllegalArgumentException("Couldn't save reverse connected table entries: $value (${e.message})", e)
                }
            }
        }
        return table.id != null
    }

    operator fun <K, V> Map<out K, V>?.contains(key: K) = this?.containsKey(key) == true

    private fun delete(table: String, column: String, id: CommonUUID?) {
        id ?: return
        database.delete(table, "$column = ${id.asSql}", null)
    }

    private fun save(table: String, contentValues: CommonContentValues): CommonUUID? {
        var changedId: CommonUUID? = null
        var changed = false
        try {
            if (!contentValues.containsKey("id")) {
                changedId = CommonUUIDObject.randomCommonUUID()
                contentValues.put("id", changedId.asByteArray)
            }
            database.insertWithOnConflict(table, "id", contentValues, CommonSQLiteDatabaseObject.CONFLICT_FAIL).toInt()
            changed = true
        } catch (e: Throwable) {
            if (changedId == null) {
                throw IllegalArgumentException("Couldn't insert values $contentValues in $table", e)
            }
        }
        if (!changed) {
            database.updateWithOnConflict(table, contentValues, "id = ${changedId.asSql}", null, CommonSQLiteDatabaseObject.CONFLICT_ROLLBACK)
                .let { if (it != 1) throw IllegalArgumentException("Couldn't update values $contentValues for $table") }
        }
        return changedId
    }

    private fun update(table: String, contentValues: CommonContentValues, id: CommonUUID?) {
        id ?: return
        val changed = database.updateWithOnConflict(table, contentValues, "id = ${id.asSql}", null, CommonSQLiteDatabaseObject.CONFLICT_ROLLBACK)
        if (changed != 1) throw IllegalArgumentException("Couldn't update values $contentValues for $table (ID: $id)")
    }

    fun <T: ComplexOrmTable, R> saveOneColumn(table: KClass<T>, column: KProperty1<T, R?>, id: CommonUUID, value: R) {
        val contentValues = CommonContentValues()
        when (value) {
            is Int -> contentValues.put(column.name.toSql(), value)
            is String -> contentValues.put(column.name.toSql(), value)
            is Long -> contentValues.put(column.name.toSql(), value)
            is Boolean -> contentValues.put(column.name.toSql(), value)
            is ByteArray -> contentValues.put(column.name.toSql(), value)
            else -> throw IllegalArgumentException("Can't save value $value (unknown type)")
        }
        contentValues.put("id", id.asByteArray)
        save(table.tableName, contentValues)
    }

    fun <T: ComplexOrmTable> changeId(table: KClass<T>, oldId: CommonUUID, newId: CommonUUID) {
        if (oldId == newId) return
        database.beginTransaction()
        try {
            val tableClass = table.longName
            val tableName = table.tableName
            val values = CommonContentValues()
            values.put(ComplexOrmTable::id.name.toSql(), newId.asByteArray)
            try {
                val feedback = database.updateWithOnConflict(tableName, values, "id = $oldId",
                        null, CommonSQLiteDatabaseObject.CONFLICT_IGNORE)
                if (feedback == -1) return
            } catch (e: Throwable) {
                if (!e.toString().contains("SQLiteConstraintException")) throw e
                database.delete(tableName, "id = $newId", null)
                val feedback = database.updateWithOnConflict(tableName, values, "id = $oldId",
                        null, CommonSQLiteDatabaseObject.CONFLICT_IGNORE)
                if (feedback == -1) return
            }
            complexOrmTableInfo.connectedColumns.forEach { connectedTable ->
                connectedTable.value.filter { it.value == tableClass }.forEach { connectedColumn ->
                    val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(connectedTable.key).first
                    val idColumnName = "${connectedColumn.key}_id"
                    val contentValues = CommonContentValues()
                    contentValues.put(idColumnName, newId.asByteArray)
                    database.updateWithOnConflict(connectedTableName, contentValues,
                            "$idColumnName = ${oldId.asSql}", null, CommonSQLiteDatabaseObject.CONFLICT_ROLLBACK)
                }
            }
            complexOrmTableInfo.joinColumns.forEach { connectedTable ->
                connectedTable.value.filter { it.value == tableClass }.forEach { connectedColumn ->
                    val idColumnName = "${tableName}_id"
                    val contentValues = CommonContentValues()
                    contentValues.put(idColumnName, newId.asByteArray)
                    database.updateWithOnConflict("${tableName}_${connectedColumn.key}", contentValues,
                            "$idColumnName = ${oldId.asSql}", null, CommonSQLiteDatabaseObject.CONFLICT_ROLLBACK)
                }
            }
            complexOrmTableInfo.reverseJoinColumns.forEach { connectedTable ->
                connectedTable.value.filter { it.value.split(';')[0] == tableClass }.forEach { connectedTableAndColumn ->
                    val (connectedTableClass, connectedColumn) = connectedTableAndColumn.value.split(';')
                    val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(connectedTableClass).first
                    val idColumnName = "${tableName}_id"
                    val contentValues = CommonContentValues()
                    contentValues.put(idColumnName, newId.asByteArray)
                    database.updateWithOnConflict("${connectedTableName}_$connectedColumn", contentValues,
                            "$idColumnName = ${oldId.asSql}", null, CommonSQLiteDatabaseObject.CONFLICT_ROLLBACK)
                }
            }
            complexOrmTableInfo.reverseConnectedColumns.forEach { connectedTable ->
                connectedTable.value.filter { it.value.split(';')[0] == tableClass }.forEach { connectedTableAndColumn ->
                    val (connectedTableClass, connectedColumn) = connectedTableAndColumn.value.split(';')
                    val connectedTableName = complexOrmTableInfo.basicTableInfo.getValue(connectedTableClass).first
                    val idColumnName = "${connectedColumn}_id"
                    val contentValues = CommonContentValues()
                    contentValues.put(idColumnName, newId.asByteArray)
                    database.updateWithOnConflict(connectedTableName, contentValues,
                            "$idColumnName = ${oldId.asSql}", null, CommonSQLiteDatabaseObject.CONFLICT_ROLLBACK)
                }
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
    inline fun <T>doInTransaction(f: () -> T, errorHandling: (Throwable) -> Unit) {
        beginTransaction()
        try {
            f().also { setTransactionSuccessful() }
        } catch (e: Throwable) {
            errorHandling(e)
        } finally {
            endTransaction()
        }
    }
    fun beginTransaction() = database.beginTransaction()
    fun setTransactionSuccessful() = database.setTransactionSuccessful()
    fun endTransaction() = database.endTransaction()
}
