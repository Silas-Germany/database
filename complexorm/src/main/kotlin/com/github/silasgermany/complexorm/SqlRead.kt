package com.github.silasgermany.complexorm

class SqlRead {
/*
    private val constructors get() = GeneratedSqlTables.constructors[interfaceName]!!

    private val normalColumns  get() = GeneratedSqlTables.normalColumns[interfaceName]!!

    private val connectedColumns get() = GeneratedSqlTables.connectedColumns[interfaceName]!!
    private val reverseConnectedColumns get() = GeneratedSqlTables.reverseConnectedColumns[interfaceName]!!
    private val joinColumns get() = GeneratedSqlTables.joinColumns[interfaceName]!!
    private val reverseJoinColumns  get() = GeneratedSqlTables.reverseJoinColumns[interfaceName]!!

    private val nextRequests = mutableMapOf<String, MutableList<SqlTable>>()
    private val notAlreadyLoaded = mutableMapOf<String, MutableList<SqlTable>>()
    private var alreadyLoaded = mutableMapOf<String, MutableMap<Int, SqlTable>>()
    private var alreadyLoadedStart = mutableMapOf<String, Map<Int, SqlTable>>()

    private lateinit var interfaceName: String
    private lateinit var restrictions: Map<String, String>

    class SqlDatabase private constructor() {
        private var androidDatabase: android.database.sqlite.SQLiteDatabase? = null
        private var cipherDatabase: SQLiteDatabase? = null
        constructor(database: android.database.sqlite.SQLiteDatabase): this() {
            androidDatabase = database
        }
        constructor(database: SQLiteDatabase): this() {
            cipherDatabase = database
        }
        fun rawQuery(query: String): Cursor = androidDatabase?.rawQuery(query, null)
                ?: cipherDatabase!!.rawQuery(query, null)
    }
    fun <T: SqlTable>android.database.sqlite.SQLiteDatabase.read2(table: KClass<T>, conditions: Map<String, String> = emptyMap(),
                                          existingEntries: MutableMap<String, MutableMap<Int, SqlTable>> = mutableMapOf())
            = SqlDatabase(this).read2(table, conditions, existingEntries)
    fun <T: SqlTable>SQLiteDatabase.read2(table: KClass<T>, conditions: Map<String, String> = emptyMap(),
                                               existingEntries: MutableMap<String, MutableMap<Int, SqlTable>> = mutableMapOf())
            = SqlDatabase(this).read2(table, conditions, existingEntries)
    private fun <T: SqlTable>SqlDatabase.read2(table: KClass<T>, conditions: Map<String, String> = emptyMap(),
                                                    existingEntries: MutableMap<String, MutableMap<Int, SqlTable>> = mutableMapOf()): List<T> {
        alreadyLoaded = existingEntries
        alreadyLoadedStart = existingEntries.toList().associateTo(mutableMapOf()) { it.first to it.second.toMap() }
        interfaceName = table.qualifiedName!!.split('.').run { get(size - 2) }
        restrictions = conditions

        val tableName = table.tableName.toLowerCase()
        LogSecure.d(this, "Read $tableName")
        //LogSecure.special("Read $restrictions;$existingEntries")
        val result = query(tableName).map { it.second }
        //LogSecure.special("${nextRequests.keys}")
        while (notAlreadyLoaded.isNotEmpty()) {
            val missingEntryTable = notAlreadyLoaded.keys.first()
            val missingEntries = notAlreadyLoaded[missingEntryTable]!!
            LogSecure.w(this, "Load additionally: ${missingEntries.map { it.id }} from $missingEntryTable")
            query(missingEntryTable, where = "WHERE $missingEntryTable.id IN (${missingEntries.joinToString { "${it.id }" }})", missingEntries = missingEntries)
            notAlreadyLoaded.remove(missingEntryTable)
        }
        while (nextRequests.isNotEmpty()) {
            val connectedTable = nextRequests.keys.first()
            //LogSecure.special("$connectedTable;${nextRequests[connectedTable]?.map { it.map }}")
            val ids = nextRequests[connectedTable]!!.map { it.id!! }.toSet()
            reverseConnectedColumns[connectedTable]?.forEach {
                val connectedTableName = it.value.first
                val connectedColumn = "${it.value.second ?: connectedTable}_id"
                val where = "WHERE $connectedTableName.${it.value.second
                        ?: connectedTable}_id IN (${ids.joinToString()})"
                //LogSecure.special("$connectedColumn;$where")
                val joinValues = query(connectedTableName, "$connectedTableName.$connectedColumn AS join_column", where)
                //LogSecure.special("$joinValues")
                val transformedValues = joinValues.groupBy { it.first }
                nextRequests[connectedTable]!!.forEach { entry ->
                    val id = entry.id!!
                    entry.map[it.key.reverseUnderScore] = transformedValues[id]?.map { value ->
                        value.second.map[(it.value.second ?: connectedTable).reverseUnderScore] = entry
                        value.second
                    }.orEmpty()
                    /*
                    entry.map[it.key.reverseUnderScore] = joinValues.mapNotNull { joinEntry ->
                        joinEntry.second.takeIf { joinEntry.first == id }
                                ?.apply { map[(it.value.second ?: connectedTable).reverseUnderScore] = entry }
                    }
                    */
                }
            }
            joinColumns[connectedTable]?.forEach {
                val connectedColumn = "${connectedTable}_id"
                val where = "LEFT JOIN ${connectedTable}_${it.key} AS join_table ON join_table.${it.value}_id = ${it.value}.id " +
                        "WHERE join_table.${connectedTable}_id IN (${ids.joinToString()})"
                //LogSecure.special("$connectedColumn;$where")
                val joinValues = query(it.value, "join_table.$connectedColumn", where)
                //LogSecure.special("$joinValues")
                nextRequests[connectedTable]!!.forEach { entry ->
                    val id = entry.id!!
                    entry.map[it.key.reverseUnderScore] = joinValues
                            .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                }
            }
            reverseJoinColumns[connectedTable]?.forEach {
                val connectedColumn = "${connectedTable}_id"
                val where = "LEFT JOIN ${it.value.first}_${it.value.second} AS join_table ON join_table.${it.value.first}_id = ${it.value.first}.id " +
                        "WHERE join_table.${connectedTable}_id IN (${ids.joinToString()})"
                //LogSecure.special("$connectedColumn;$where")
                val joinValues = query(it.value.first, "join_table.$connectedColumn", where)
                //LogSecure.special("$joinValues")
                nextRequests[connectedTable]!!.forEach { entry ->
                    val id = entry.id!!
                    entry.map[it.key.reverseUnderScore] = joinValues
                            .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                }
            }
            nextRequests.remove(connectedTable)
        }
        @Suppress("UNCHECKED_CAST")
        return (result as List<T>)
                //.apply { map { it.showAll().split('}').forEach {LogSecure.special(it)} } }
                //.apply { LogSecure.special("${map { it.showAll() }}") }
                .apply { LogSecure.d(this, "Loading finished") }
    }

    private fun SqlDatabase.query(tableName: String, connectedColumn: String? = null,
                                  where: String? = null, missingEntries: List<SqlTable>? = null): List<Pair<Int?, SqlTable>> {
        val columns = mutableListOf("$tableName.id")
        columns.addColumns(tableName, missingEntries != null)
        connectedColumn?.let { columns.add(it) }
        val joins = mutableListOf<String>()
        joins.addJoins(tableName, missingEntries != null)
        where?.let { joins.add(it) } ?: joins.add("WHERE 1!=0")
//        if (joins.isEmpty()) joins.add("WHERE 1!=0")
        restrictions[tableName]?.let { joins += "AND $it" }
        val query = "SELECT ${columns.joinToString()} FROM $tableName ${joins.joinToString(" ")};"
        LogSecure.d(this, query)
        val result = mutableListOf<Pair<Int?, SqlTable>>()
        rawQuery(query).forEach {
            readIndex = 0
            val (connectedId, databaseEntry) = it.readColumns(tableName, missingEntries, connectedColumn)
            if (databaseEntry != null) {
                //LogSecure.special("$tableName:${alreadyLoaded.init(tableName)[databaseEntry.id!!]?.hashCode()};${databaseEntry.hashCode()}")
                alreadyLoaded.init(tableName)[databaseEntry.id!!] = databaseEntry
                result.add(connectedId to databaseEntry)
            }
        }
        alreadyLoadedStart[tableName] = alreadyLoaded.init(tableName)
        return result
    }

    private fun databaseMapInit(id: Int?) = mutableMapOf<String, Any?>("id" to id).run {
        withDefault {
            throw IllegalAccessException("Key does not exist: $it in $this")
        }
    }

    private fun Cursor.getValue(index: Int, type: String): Any? =
            if (isNull(index)) null
            else when (type) {
                "boolean" -> getLong(index) != 0L
                "int" -> getInt(index)
                "long" -> getLong(index)
                "java.lang.Integer" -> getInt(index)
                "java.lang.Long" -> getLong(index)
                "float" -> getFloat(index)
                "java.lang.String" -> getString(index)
                "byte[]" -> getBlob(index)
                "org.threeten.bp.LocalDate" -> parse(getString(index), DateTimeFormatter.ISO_DATE)
                else -> throw IllegalArgumentException("Couldn't find type $type (${getType(index)})")
            }

    private fun MutableList<String>.addColumns(tableName: String, isMissing: Boolean, columnName: String? = null) {
        if (alreadyLoadedStart[tableName] != null && !isMissing) return
        normalColumns[tableName]?.forEach { add("${columnName ?: tableName}.${it.key}") }
        connectedColumns[tableName]?.forEach { joinTable ->
            if (reverseConnectedColumns[joinTable.last]?.any { it.value.first == tableName } == true &&
                    alreadyLoadedStart[joinTable.last] == null) return@forEach
            if (alreadyLoadedStart[joinTable.last] != null && !isMissing) {
                add("${columnName ?: tableName}.${joinTable.key}_id")
                return@forEach
            }
            add("${joinTable.key}.id")
            addColumns(joinTable.last, isMissing, joinTable.key)
        }
    }

    private var readIndex = 0
    private fun Cursor.readColumns(tableName: String, missingEntries: List<SqlTable>?, connectedColumn: String? = null): Pair<Int?, SqlTable?> {
        val id = getValue(readIndex++, "int")
        //LogSecure.special("READ column: $id;$tableName;$connectedColumn;$readIndex${normalColumns[tableName]}${connectedColumns[tableName]}")
        val databaseMap = databaseMapInit(id as Int?)
        val alreadyLoadedTable = alreadyLoadedStart[tableName]
        if (alreadyLoadedTable != null && missingEntries == null) {
            val connectedId = connectedColumn?.let { getValue(readIndex++, "int") as Int }
            alreadyLoaded[tableName]?.get(id)?.let { return connectedId to it }
            return connectedId to (alreadyLoadedTable[id as Int] ?: constructors[tableName]!!.invoke(databaseMap)
                    .also { notAlreadyLoaded.init(tableName).add(it) })
        }
        //LogSecure.special("Check join for $tableName: ${joinColumns[tableName]};$alreadyLoaded")
        normalColumns[tableName]?.forEach {
            val value = getValue(readIndex++, it.value)
            databaseMap[it.key.reverseUnderScore] = value
        }
        connectedColumns[tableName]?.forEach { joinTableNames ->
            if (reverseConnectedColumns[joinTableNames.last]?.any { it.value.first == tableName } == true &&
                            alreadyLoadedStart[joinTableNames.last] == null) return@forEach
            val (_, databaseEntry) = readColumns(joinTableNames.last, null)
            //LogSecure.special("$tableName;$x;${databaseEntry?.showWithIds()}${(*databaseEntry) as Int}")
            //LogSecure.special("${databaseEntry.map}")
            if (databaseEntry != null) {
                //LogSecure.special("ID:${databaseEntry.map}")
                alreadyLoaded.init(joinTableNames.last)[databaseEntry.id!!] = databaseEntry
                val identifier = joinTableNames.key.reverseUnderScore
                if (databaseMap[identifier] == null) databaseMap[identifier] = databaseEntry
                else return null to databaseMap[identifier] as SqlTable
            } else databaseMap[joinTableNames.key.reverseUnderScore] = null
        }
        val connectedId = connectedColumn?.let { getValue(readIndex++, "int") as Int }
        //LogSecure.special("Check join for $tableName: ${alreadyLoaded[tableName]};${missingEntries == null}")
        if (missingEntries == null) alreadyLoaded[tableName]?.get(id)?.let { return connectedId to it }
        //LogSecure.special("$i;$columnCount;${tables[tableName]};${connectedColumns[tableName]};${columnNames.toList()};$tableName")
        //LogSecure.special("$databaseMap;$tableName")
        //if (i < columnCount) databaseMap["join_id"] = getValue(i)
        if (id == null) return connectedId to null
        val databaseEntry = if (missingEntries == null) constructors[tableName]!!.invoke(databaseMap)
        else missingEntries.find { it.id == id }!!.apply { map.putAll(databaseMap) }
        reverseConnectedColumns[tableName]?.forEach { _ ->
            nextRequests.init(tableName).add(databaseEntry)
        }
        joinColumns[tableName]?.asIterable()?.forEach { _ ->
            nextRequests.init(tableName).add(databaseEntry)
        }
        reverseJoinColumns[tableName]?.forEach { _ ->
            nextRequests.init(tableName).add(databaseEntry)
        }
        return connectedId to databaseEntry
    }

    private fun MutableList<String>.addJoins(tableName: String, isMissing: Boolean, columnName: String? = null) {
        if (alreadyLoadedStart[tableName] != null && !isMissing) return
        connectedColumns[tableName]?.forEach { joinTable ->
            if (alreadyLoadedStart[joinTable.last] != null && !isMissing) return@forEach
            var where = if (joinTable.last == "translation") {
                "LEFT JOIN translation AS ${joinTable.key} " +
                        "ON ${joinTable.key.removeSuffix("_translation")}_id = ${joinTable.key}.translation_code_id " +
                        "AND ${(restrictions["translation"]!!).replace("translation.", "${joinTable.key}.")}"
                        //"AND ${(restrictions["translation"] ?: "translation.language_id = 1").replace("translation.", "${joinTable.key}.")}"
            } else {
                "LEFT JOIN ${joinTable.last} AS ${joinTable.key} " +
                        "ON ${joinTable.key}.id = ${columnName ?: tableName}.${joinTable.key}_id"
            }
            restrictions[joinTable.last]?.let { where = where.removePrefix("LEFT ") + " AND ${it.replace("${joinTable.last}.", "${joinTable.key}.")}" }
            add(where)
            addJoins(joinTable.last, isMissing, joinTable.key)
        }
    }

    private val Map.Entry<String, String?>.last get() = value?: key
    private fun <T, K, V> MutableMap<T, MutableMap<K, V>>.init(key: T) = getOrPut(key) { mutableMapOf() }
    private fun <T, K> MutableMap<T, MutableList<K>>.init(key: T) = getOrPut(key) { mutableListOf() }
    //private val Pair<String, String?>.last get() = second?: first

    */
}