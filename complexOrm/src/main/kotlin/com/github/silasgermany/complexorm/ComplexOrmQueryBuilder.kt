package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ComplexOrmQueryBuilder internal constructor(private val complexOrmReader: ComplexOrmReader,
                             private val complexOrmTableInfo: ComplexOrmTableInfoInterface) {

    private val basicTableInfo = complexOrmTableInfo.basicTableInfo
    private val normalColumns = complexOrmTableInfo.normalColumns
    private val connectedColumns = complexOrmTableInfo.connectedColumns
    private val KClass<out ComplexOrmTable>.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(java.canonicalName!!).first

    private val restrictions = mutableMapOf<String, String>()
    private val existingEntries = mutableMapOf<String, MutableMap<Int, ComplexOrmTable>>()

    private inline fun <reified T : ComplexOrmTable> specialWhere(
        selection: String,
        vararg selectionArguments: Pair<KProperty1<T, Any?>, Any?>
    ): ComplexOrmQueryBuilder = where(T::class, selectionArguments[0].first, selection, *selectionArguments)

    inline fun <reified T : ComplexOrmTable> specialWhere(
        column: KProperty1<T, Any?>, selection: String,
        vararg selectionArguments: Any?
    ): ComplexOrmQueryBuilder = where(T::class, column, selection, *selectionArguments)

    inline fun <reified T : ComplexOrmTable> where(column: KProperty1<T, Any?>, equals: Any?): ComplexOrmQueryBuilder =
        where(T::class, column, null, equals)
    fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.where(
        table: KClass<T>, column: KProperty1<T, Any?>,
        selection: String?, vararg selectionArguments: Any?
    ): ComplexOrmQueryBuilder {
        var columnName = column.name.replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
        val rootTableName = basicTableInfo.getValue(table.java.canonicalName!!).second
        if (connectedColumns[rootTableName]?.contains(columnName) == true) columnName += "_id"
        else if (normalColumns[rootTableName]?.contains(columnName) != true)
            throw IllegalArgumentException("Can't create restriction for join columns (column $columnName from ${table.java.canonicalName}). " +
                    "Please restrict the target table instead")
        var where = (selection?.let { "($it)" } ?: "??=?")
        if (selectionArguments.isEmpty()) where = where.replace("??", "$$.\"$columnName\"")
        selectionArguments.forEach { whereArgument ->
            val transformedWhereArgument = when (whereArgument) {
                is Boolean -> if (whereArgument) "1" else "0"
                is String -> {
                    if ('%' in whereArgument)
                        where = where
                                .replace("!= ?", " NOT LIKE ?")
                                .replace("!=?", " NOT LIKE ?")
                                .replace("= ?", " LIKE ?")
                                .replace("=?", " LIKE ?")
                    "'$whereArgument'"
                }
                is Int, is Long -> "$whereArgument"
                is Enum<*> -> "${whereArgument.ordinal}"
                is Collection<*> -> {
                    if (whereArgument.any { it == null }) {
                        where = when {
                            whereArgument.any { it is String } ->
                                where.replace("??", "COALESCE(??, 'NULL')")
                            whereArgument.any { it is Enum<*> || it is Int || it is Long || it is ComplexOrmTable } ->
                                where.replace("??", "COALESCE(??, -1)")
                            !whereArgument.any { it != null } ->
                                where
                                        .replace("!=?", " IS NOT ?")
                                        .replace("!= ?", " IS NOT ?")
                                        .replace("=?", " IS ?")
                                        .replace("= ?", " IS ?")
                            else -> throw IllegalArgumentException("Collection it not of type String, Int or null")
                        }
                    }
                    where = where
                            .replace("!=?", " NOT IN (?)")
                            .replace("!= ?", " NOT IN (?)")
                            .replace("=?", " IN (?)")
                            .replace("= ?", " IN (?)")
                    when {
                        whereArgument.any { it is Enum<*> || it is Int || it is Long } ->
                            whereArgument.joinToString { it?.toString() ?: "-1" }
                        whereArgument.any { it is String } ->
                            whereArgument.joinToString { it?.run { "'$this'" } ?: "'NULL'" }
                        !whereArgument.any { it != null } -> "NULL"
                        whereArgument.any { it is ComplexOrmTable } ->
                            whereArgument.joinToString { (it as? ComplexOrmTable)?.id?.toString() ?: "-1" }
                        else -> throw IllegalArgumentException("Collection it not of type String, Int or null")
                    }
                }
                is ComplexOrmTable -> "${whereArgument.id}"
                null -> {
                    where = where
                        .replace("!= ?", " IS NOT ?")
                        .replace("!=?", " IS NOT ?")
                        .replace("= ?", " IS ?")
                        .replace("=?", " IS ?")
                    "NULL"
                }
                else -> throw IllegalArgumentException("Can't create restriction with type of $whereArgument (${whereArgument::class})")
            }
            where = where
                    .replace("??", "$$.\"$columnName\"")
                    .replaceFirst("?", transformedWhereArgument)
        }
        restrictions[table.java.canonicalName!!] = if (table.java.canonicalName !in restrictions) where
        else "${restrictions[table.java.canonicalName!!]} AND $where"
        return this@ComplexOrmQueryBuilder
    }

    inline fun <reified T : ComplexOrmTable> alreadyLoaded(entries: Collection<T>) = alreadyLoaded(T::class, entries)
    fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.alreadyLoaded(table: KClass<T>, entries: Collection<T>): ComplexOrmQueryBuilder {
        existingEntries[table.java.canonicalName!!] = entries
            .associateTo(mutableMapOf()) { it.id!! to it }
        return this@ComplexOrmQueryBuilder
    }

    inline fun <reified T : ComplexOrmTable> get(): List<T> = get(T::class)

    fun <T : ComplexOrmTable> get(table: KClass<T>): List<T> {
        val readTableInfo = ReadTableInfo(restrictions, existingEntries, complexOrmTableInfo)
        return complexOrmReader.read(table, readTableInfo)
    }

    inline fun <reified T : ComplexOrmTable> get(id: Int?): T? = get(T::class, id)
    fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.get(table: KClass<T>, id: Int?): T? {
        id ?: return null
        val tableName = table.tableName
        this@ComplexOrmQueryBuilder.restrictions[tableName] = if (tableName in restrictions) "$tableName._id = $id"
        else "${restrictions[tableName]} AND $tableName._id = $id"
        val readTableInfo = ReadTableInfo(restrictions, existingEntries, complexOrmTableInfo)
        return complexOrmReader.read(table, readTableInfo).getOrNull(0)
    }
}
