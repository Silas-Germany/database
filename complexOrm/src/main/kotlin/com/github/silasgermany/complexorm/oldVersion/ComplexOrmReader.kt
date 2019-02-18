package com.github.silasgermany.complexorm.oldVersion

class ComplexOrmReader(private val database: ComplexOrmDatabase): ComplexOrmUtils() {

    /*
    constructor(databaseFile: File) : this(ComplexOrmDatabase(databaseFile))

    private val restrictions = mutableMapOf<String, String>()
    private val existingEntries = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()

    inline fun <reified T : ComplexOrmTable> specialWhere(
        column: KProperty1<T, Any?>, selection: String,
        vararg selectionArguments: Any?
    ): ComplexOrmReader = where(T::class, column, selection, *selectionArguments)

    inline fun <reified T : ComplexOrmTable> where(column: KProperty1<T, Any?>, equals: Any?): ComplexOrmReader =
        where(T::class, column, null, equals)

    fun <T : ComplexOrmTable> ComplexOrmReader.where(
        table: KClass<T>, column: KProperty1<T, Any?>,
        selection: String?, vararg selectionArguments: Any?
    ): ComplexOrmReader {
        val tableName = table.tableName.toLowerCase()
        val columnName = "$tableName.${column.columnName.toLowerCase()}"
        var where = (selection?.let { "($it)" } ?: "?? = ?")
        if (selectionArguments.isEmpty()) where = where.replace("??", columnName)
        selectionArguments.forEach { whereArgument ->
            val transformedWhereArgument = when (whereArgument) {
                is String -> {
                    if (whereArgument.contains('%'))
                        where = where.replace(" = ?", " LIKE ?")
                    "'$whereArgument'"
                }
                is Int -> "$whereArgument"
                is Enum<*> -> "${whereArgument.ordinal}"
                is Collection<*> -> {
                    if (whereArgument.any { it == null }) {
                        where = when {
                            whereArgument.any { it is String } ->
                                where.replace("??", "COALESCE(??, 'NULL')")
                            whereArgument.any { it is Int } ->
                                where.replace("??", "COALESCE(??, -1)")
                            !whereArgument.any { it != null } ->
                                where.replace(" = ?", " IS ?")
                            else -> throw IllegalArgumentException("Collection it not of type String, Int or null")
                        }
                    }
                    where = where.replace(" = ?", " IN (?)").replace(" != ?", " NOT IN (?)")
                    when {
                        whereArgument.any { it is Int } ->
                            whereArgument.joinToString { it?.run { toString() } ?: "-1" }
                        whereArgument.any { it is String } ->
                            whereArgument.joinToString { it?.run { "'$this'" } ?: "'NULL'" }
                        !whereArgument.any { it != null } -> "NULL"
                        whereArgument.any { it is ComplexOrmTable } ->
                            whereArgument.joinToString { (it as? ComplexOrmTable)?.id?.toString() ?: "-1" }
                        else -> throw IllegalArgumentException("Collection it not of type String, Int or null")
                    }
                }
                null -> {
                    where = where
                        .replace(" != ?", " IS NOT ?")
                        .replace(" = ?", " IS ?")
                    "NULL"
                }
                else -> throw IllegalArgumentException("Couldn't find type of $whereArgument")
            }
            where = where.replace("??", columnName)
                .replaceFirst("?", transformedWhereArgument)
        }
        restrictions[tableName] = if (restrictions[tableName] == null) where
        else "${restrictions[tableName]} AND $where"
        return this@ComplexOrmReader
    }

    inline fun <reified T : ComplexOrmTable> alreadyLoaded(entries: Collection<T>) = alreadyLoaded(T::class, entries)
    fun <T : ComplexOrmTable> ComplexOrmReader.alreadyLoaded(table: KClass<T>, entries: Collection<T>): ComplexOrmReader {
        existingEntries[table.tableName.toLowerCase()] = entries
            .associateTo(mutableMapOf()) { it.id!! to it }
        return this@ComplexOrmReader
    }

    fun <T : ComplexOrmTable> get(table: KClass<T>): List<T> {
        return database.read(table, existingEntries)
    }

    inline fun <reified T : ComplexOrmTable> get(): List<T> = get(T::class)

    fun <T : ComplexOrmTable> ComplexOrmReader.get(table: KClass<T>, id: Int?): T? {
        id ?: return null
        val tableName = table.tableName.toLowerCase()
        this@ComplexOrmReader.restrictions[tableName] = if (restrictions[tableName] == null) "$tableName._id = $id"
        else "${restrictions[tableName]} AND $tableName._id = $id"
        return database.read(table, existingEntries)
            .getOrNull(0)
    }

    inline fun <reified T : ComplexOrmTable> get(id: Int?): T? = get(T::class, id)

    private val constructors get() = complexOrmTables.constructors[interfaceName]
    private val normalColumns get() = complexOrmTables.normalColumns[interfaceName]
    private val connectedColumns get() = complexOrmTables.connectedColumns[interfaceName]
    private val reverseConnectedColumns get() = complexOrmTables.reverseConnectedColumns[interfaceName]
    private val joinColumns get() = complexOrmTables.joinColumns[interfaceName]
    //private val reverseJoinColumns  get() = ComplexOrmTables.joinColumns[interfaceName]//ComplexOrmTables.reverseJoinColumns[interfaceName]!!

    private val nextRequests = mutableMapOf<String, MutableList<ComplexOrmTable>>()
    private val notAlreadyLoaded = mutableMapOf<String, MutableList<ComplexOrmTable>>()
    private var alreadyLoaded = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()
    private var alreadyLoadedStart = mutableMapOf<String, Map<Long, ComplexOrmTable>>()

    private lateinit var interfaceName: String

    private fun <T : ComplexOrmTable> ComplexOrmDatabase.read(
        table: KClass<T>,
        existingEntries: MutableMap<String, MutableMap<Long, ComplexOrmTable>> = mutableMapOf()
    ): List<T> {
        alreadyLoaded = existingEntries
        alreadyLoadedStart = existingEntries.toList().associateTo(mutableMapOf()) { it.first to it.second.toMap() }
        interfaceName = table.qualifiedName!!.split('.').run { get(size - 2) }

        val tableName = table.tableName


        val result = query(tableName, null).map { it.second }

        while (notAlreadyLoaded.isNotEmpty()) {
            val missingEntryTable = notAlreadyLoaded.keys.first()
            val missingEntries = notAlreadyLoaded[missingEntryTable]!!

            query(
                missingEntryTable,
                where = "WHERE $missingEntryTable._id IN (${missingEntries.joinToString { "${it.id}" }})",
                missingEntries = missingEntries
            )
            notAlreadyLoaded.remove(missingEntryTable)
        }
        while (nextRequests.isNotEmpty()) {
            val connectedTable = nextRequests.keys.first()

            val ids = nextRequests[connectedTable]!!.map { it.id!! }.toSet()
            reverseConnectedColumns?.get(connectedTable)?.forEach {
                val connectedTableName = it.value.first
                val connectedColumn = "${it.value.second ?: connectedTable}_id"
                val where = "WHERE $connectedTableName.${it.value.second
                    ?: connectedTable}_id IN (${ids.joinToString()})"

                val joinValues = query(connectedTableName, "$connectedTableName.$connectedColumn AS join_column", where)

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
            joinColumns?.get(connectedTable)?.forEach {
                val connectedColumn = "${connectedTable}_id"
                val where =
                    "LEFT JOIN ${connectedTable}_${it.key} AS join_table ON join_table.${it.value}_id = ${it.value}._id " +
                            "WHERE join_table.${connectedTable}_id IN (${ids.joinToString()})"

                val joinValues = query(it.value, "join_table.$connectedColumn", where)

                nextRequests[connectedTable]!!.forEach { entry ->
                    val id = entry.id!!
                    entry.map[it.key.reverseUnderScore] = joinValues
                        .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                }
            }
            /*
            reverseJoinColumns?.get(connectedTable)?.forEach {
                val connectedColumn = "${connectedTable}_id"
                val where =
                    "LEFT JOIN ${it.value.first}_${it.value.second} AS join_table ON join_table.${it.value.first}_id = ${it.value.first}.id " +
                            "WHERE join_table.${connectedTable}_id IN (${ids.joinToString()})"

                val joinValues = query(it.value.first, "join_table.$connectedColumn", where)

                nextRequests[connectedTable]!!.forEach { entry ->
                    val id = entry.id!!
                    entry.map[it.key.reverseUnderScore] = joinValues
                        .mapNotNull { joinEntry -> joinEntry.second.takeIf { joinEntry.first == id } }
                }
            }
            */
            nextRequests.remove(connectedTable)
        }
        @Suppress("UNCHECKED_CAST")
        return (result as List<T>)


            .apply {
            }
    }

    private fun ComplexOrmDatabase.query(
        tableName: String, connectedColumn: String? = null,
        where: String? = null, missingEntries: List<ComplexOrmTable>? = null
    ): List<Pair<Long?, ComplexOrmTable>> {
        val columns = mutableListOf("$tableName._id")
        columns.addColumns(tableName, missingEntries != null)
        connectedColumn?.let { columns.add(it) }
        val joins = mutableListOf<String>()
        joins.addJoins(tableName, missingEntries != null)
        where?.let { joins.add(it) } ?: joins.add("WHERE 1")
//        if (joins.isEmpty()) joins.add("WHERE 1!=0")
        restrictions[tableName]?.let { joins += "AND $it" }
        val query = "SELECT ${columns.joinToString()} FROM $tableName ${joins.joinToString(" ")};"

        val result = mutableListOf<Pair<Long?, ComplexOrmTable>>()
        queryForEach(query) {
            readIndex = 0
            val (connectedId, databaseEntry) = it.readColumns(tableName, missingEntries, connectedColumn)
            if (databaseEntry != null) {
                alreadyLoaded.init(tableName)[databaseEntry.id!!] = databaseEntry
                result.add(connectedId to databaseEntry)
            }
        }
        alreadyLoadedStart[tableName] = alreadyLoaded.init(tableName)
        return result
    }

    private fun databaseMapInit(id: Long?) = mutableMapOf<String, Any?>("_id" to id).run {
        withDefault {
            throw IllegalAccessException("Key does not exist: $it in $this")
        }
    }

    private fun Cursor.getValue(index: Int, type: ComplexOrmTypes): Any? =
        if (isNull(index)) null
        else when (type) {
            ComplexOrmTypes.Boolean -> getLong(index) != 0L
            ComplexOrmTypes.Int -> getInt(index)
            ComplexOrmTypes.Long -> getLong(index)
            //"float" -> getFloat(index)
            ComplexOrmTypes.String -> getString(index)
            ComplexOrmTypes.ByteArray -> getBlob(index)
            //"org.threeten.bp.LocalDate" -> parse(getString(index), DateTimeFormatter.ISO_DATE)
            else -> throw IllegalArgumentException("Couldn't find type $type (${getType(index)})")
        }

    private fun MutableList<String>.addColumns(tableName: String, isMissing: Boolean, columnName: String? = null) {
        if (alreadyLoadedStart[tableName] != null && !isMissing) return
        normalColumns?.get(tableName)?.forEach { add("${columnName ?: tableName}.${it.key}") }
        connectedColumns?.get(tableName)?.forEach { joinTable ->
            if (reverseConnectedColumns?.get(joinTable.last)?.any { it.value.first == tableName } == true &&
                alreadyLoadedStart[joinTable.last] == null) return@forEach
            if (alreadyLoadedStart[joinTable.last] != null && !isMissing) {
                add("${columnName ?: tableName}.${joinTable.key}_id")
                return@forEach
            }
            add("${joinTable.key}._id")
            addColumns(joinTable.last, isMissing, joinTable.key)
        }
    }

    private var readIndex = 0
    private fun Cursor.readColumns(
            tableName: String,
            missingEntries: List<ComplexOrmTable>?,
            connectedColumn: String? = null
    ): Pair<Long?, ComplexOrmTable?> {
        val id = getValue(readIndex++, ComplexOrmTypes.Long) as Long?

        val databaseMap = databaseMapInit(id)
        val alreadyLoadedTable = alreadyLoadedStart[tableName]
        if (alreadyLoadedTable != null && missingEntries == null) {
            val connectedId = connectedColumn?.let { getValue(readIndex++, ComplexOrmTypes.Long) as Long }
            alreadyLoaded[tableName]?.get(id)?.let { return connectedId to it }
            return connectedId to (alreadyLoadedTable[id] ?: constructors?.get(tableName)!!.invoke(databaseMap)
                .also { notAlreadyLoaded.init(tableName).add(it) })
        }

        normalColumns?.get(tableName)?.forEach {
            val value = getValue(readIndex++, it.value)
            databaseMap[it.key.reverseUnderScore] = value
        }
        connectedColumns?.get(tableName)?.forEach { joinTableNames ->
            if (reverseConnectedColumns?.get(joinTableNames.last)?.any { it.value.first == tableName } == true &&
                alreadyLoadedStart[joinTableNames.last] == null) return@forEach
            val (_, databaseEntry) = readColumns(joinTableNames.last, null)
            if (databaseEntry != null) {
                alreadyLoaded.init(joinTableNames.last)[databaseEntry.id!!] = databaseEntry
                val identifier = joinTableNames.key.reverseUnderScore
                if (databaseMap[identifier] == null) databaseMap[identifier] = databaseEntry
                else return null to databaseMap[identifier] as ComplexOrmTable
            } else databaseMap[joinTableNames.key.reverseUnderScore] = null
        }
        val connectedId = connectedColumn?.let { getValue(readIndex++, ComplexOrmTypes.Long) as Long }

        if (missingEntries == null) alreadyLoaded[tableName]?.get(id)?.let { return connectedId to it }

        //if (i < columnCount) databaseMap["join_id"] = getValue(i)
        if (id == null) return connectedId to null
        val databaseEntry = if (missingEntries == null) constructors?.get(tableName)!!.invoke(databaseMap)
        else missingEntries.find { it.id == id }!!.apply { map.putAll(databaseMap) }
        reverseConnectedColumns?.get(tableName)?.forEach { _ ->
            nextRequests.init(tableName).add(databaseEntry)
        }
        joinColumns?.get(tableName)?.asIterable()?.forEach { _ ->
            nextRequests.init(tableName).add(databaseEntry)
        }
        /*
        reverseJoinColumns?.get(tableName)?.forEach { _ ->
            nextRequests.init(tableName).add(databaseEntry)
        }
        */
        return connectedId to databaseEntry
    }

    private fun MutableList<String>.addJoins(tableName: String, isMissing: Boolean, columnName: String? = null) {
        if (alreadyLoadedStart[tableName] != null && !isMissing) return
        connectedColumns?.get(tableName)?.forEach { joinTable ->
            if (alreadyLoadedStart[joinTable.last] != null && !isMissing) return@forEach
            var where = if (joinTable.last == "translation") {
                "LEFT JOIN translation AS ${joinTable.key} " +
                        "ON ${joinTable.key.removeSuffix("_translation")}_id = ${joinTable.key}.translation_code_id " +
                        "AND ${(restrictions["translation"]!!).replace("translation.", "${joinTable.key}.")}"
                //"AND ${(restrictions["translation"] ?: "translation.language_id = 1").replace("translation.", "${joinTable.key}.")}"
            } else {
                "LEFT JOIN ${joinTable.last} AS ${joinTable.key} " +
                        "ON ${joinTable.key}._id = ${columnName ?: tableName}.${joinTable.key}_id"
            }
            restrictions[joinTable.last]?.let {
                where = where.removePrefix("LEFT ") + " AND ${it.replace("${joinTable.last}.", "${joinTable.key}.")}"
            }
            add(where)
            addJoins(joinTable.last, isMissing, joinTable.key)
        }
    }

    private val Map.Entry<String, String?>.last get() = value ?: key
    private fun <T, K, V> MutableMap<T, MutableMap<K, V>>.init(key: T) = getOrPut(key) { mutableMapOf() }
    private fun <T, K> MutableMap<T, MutableList<K>>.init(key: T) = getOrPut(key) { mutableListOf() }
    //private val Pair<String, String?>.last get() = second?: first
    // */
}