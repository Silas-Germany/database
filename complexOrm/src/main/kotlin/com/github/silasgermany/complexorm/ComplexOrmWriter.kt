package com.github.silasgermany.complexorm

class ComplexOrmWriter(private val database: ComplexOrmDatabase): ComplexOrmUtils() {

    /*
    constructor(databaseFile: File) : this(ComplexOrmDatabase(databaseFile))

    private fun getIdentifier(tableClass: KClass<*>) = tableClass.java.name
        .run { substring(lastIndexOf('.') + 1).split('$') }.let { it[0] to it[1].sql }

    private fun getNormalColumns(identifier: Pair<String, String>) = mapOf("_id" to ComplexOrmTypes.Long)
        .plus(identifier.run { complexOrmTables.normalColumns[first]?.get(second) } ?: emptyMap())
    private fun joinColumns(identifier: Pair<String, String>) = identifier.run { complexOrmTables.joinColumns[first]?.get(second) }
    private fun connectedColumn(identifier: Pair<String, String>) = identifier.run { complexOrmTables.connectedColumns[first]?.get(second) }
    private fun reverseConnectedColumn(identifier: Pair<String, String>) = identifier.run { complexOrmTables.reverseConnectedColumns[first]?.get(second) }

    fun write(table: ComplexOrmTable): Long? {
        return database.use {
                complexOrmSchema.dropTableCommands.forEach { rawComplexOrm(it) }
                complexOrmSchema.createTableCommands.forEach { rawComplexOrm(it) }
            write2(table)
        }
    }

    private fun ComplexOrmDatabase.write2(table: ComplexOrmTable): Long? {
        Log.e("DATABASE", "Save $table")
        val contentValues = ContentValues()
        val identifier = getIdentifier(table::class)
        val normalColumns = getNormalColumns(identifier)
        val joinColumns = joinColumns(identifier)
        val connectedColumn = connectedColumn(identifier)
        val reverseConnectedColumn = reverseConnectedColumn(identifier)
        table.map.forEach { (key, value) ->
            val ComplexOrmKey = key.sql
            normalColumns.get(ComplexOrmKey)?.also {
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
            connectedColumn?.get(ComplexOrmKey)?.let {
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
            val ComplexOrmKey = key.sql
            joinColumns?.get(ComplexOrmKey)?.let { joinTable ->
                try {
                    delete("${identifier.second}_$ComplexOrmKey", "${identifier.second}_id = ${table.id}")
                    val innerContentValues = ContentValues()
                    innerContentValues.put("${identifier.second}_id", table.id)
                    (value as List<*>).forEach { joinTableEntry ->
                        joinTableEntry as ComplexOrmTable
                        if (joinTableEntry.id == null) write2(joinTableEntry)
                        innerContentValues.put("${joinTable}_id", joinTableEntry.id)
                        save("${identifier.second}_$ComplexOrmKey", innerContentValues)
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Couldn't save joined table entries: $value (${e.message})")
                }
            }
            reverseConnectedColumn?.get(ComplexOrmKey)?.let { joinTableData ->
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
        return table.id
    }

    private fun ComplexOrmDatabase.save(table: String, contentValues: ContentValues): Long {
        Log.e("DATABASE", "Insert in table $table: ${contentValues.valueSet()}")
        return insertOrThrow(table, contentValues)
    }
    // */

}
