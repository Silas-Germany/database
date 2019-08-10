package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.IdType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class ComplexOrmQueryBuilder internal constructor(private val complexOrmReader: ComplexOrmReader,
                                                       private val complexOrmTableInfo: ComplexOrmTableInfoInterface) {

    private val IdType.asSql get() = "x'${toString().replace("-", "")}'"

    private val basicTableInfo = complexOrmTableInfo.basicTableInfo
    private val normalColumns = complexOrmTableInfo.normalColumns
    private val connectedColumns = complexOrmTableInfo.connectedColumns
    @Suppress("unused")
    private val KClass<out ComplexOrmTable>.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(longName).first

    private val restrictions = mutableMapOf<String, String>()
    private val existingEntries = mutableMapOf<String, MutableMap<IdType, ComplexOrmTable>>()

    @Suppress("unused")
    private inline fun <reified T : ComplexOrmTable> specialWhere(
        selection: String,
        vararg selectionArguments: Pair<KProperty1<T, Any?>, Any?>
    ): ComplexOrmQueryBuilder = where(T::class, selectionArguments[0].first, selection, *selectionArguments)

    @Suppress("unused")
    inline fun <reified T : ComplexOrmTable> specialWhere(
        column: KProperty1<T, Any?>, selection: String,
        vararg selectionArguments: Any?
    ) = where(T::class, column, selection, *selectionArguments)

    // Has to come after the where commands
    inline fun <reified T: ComplexOrmTable> orderBy(column: KProperty1<T, Any?>) =
        specialWhere(column, "1) ORDER BY (LOWER(??)")

    @Suppress("unused")
    inline fun <reified T : ComplexOrmTable> whereNotNull(column: KProperty1<T, Any?>) =
        where(T::class, column, "??!=?", null)
    inline fun <reified T : ComplexOrmTable> where(column: KProperty1<T, Any?>, equals: Any?) =
        if (equals is Sequence<*>) where(T::class, column, null, equals.toList())
        else where(T::class, column, null, equals)
    open fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.where(
        table: KClass<T>, column: KProperty1<T, Any?>,
        selection: String?, vararg selectionArguments: Any?
    ) = where(table, column.name, selection, *selectionArguments)
    open fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.where(
        table: KClass<T>, columnPropertyName: String,
        selection: String?, vararg selectionArguments: Any?
    ): ComplexOrmQueryBuilder {
        var columnName = columnPropertyName.replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
        val rootTableName = basicTableInfo.getValue(table.longName).second
        if (connectedColumns[rootTableName]?.contains(columnName) == true) columnName += "_id"
        else if (columnName != "id" && normalColumns[rootTableName]?.contains(columnName) != true)
            throw IllegalArgumentException("Can't create restriction for join columns (column $columnName from ${table.longName}). " +
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
                is IdType -> whereArgument.asSql
                is Collection<*> -> {
                    if (whereArgument.any { it == null }) {
                        where = when {
                            whereArgument.any { it is String } ->
                                where.replace("??", "COALESCE(??, 'NULL')")
                            whereArgument.any { it is Enum<*> || it is Int || it is Long || it is ComplexOrmTable } ->
                                where.replace("??", "COALESCE(??, -1)")
                            whereArgument.any { it is IdType } ->
                                where.replace("??", "COALESCE(??, x'')")
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
                        whereArgument.any { it is IdType } ->
                            whereArgument.joinToString { it?.let { (it as IdType).asSql } ?: "x''" }
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
        restrictions[table.longName] = if (table.longName !in restrictions) where
        else "${restrictions[table.longName]} AND $where"
        return this@ComplexOrmQueryBuilder
    }

    @Suppress("unused")
    inline fun <reified T : ComplexOrmTable> alreadyLoaded(entries: Collection<T?>?) = alreadyLoaded(T::class, entries)
    inline fun <reified T : ComplexOrmTable> alreadyLoaded(entries: Sequence<T?>?) = alreadyLoaded(T::class, entries?.toList())
    open fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.alreadyLoaded(table: KClass<T>, entries: Collection<T?>?): ComplexOrmQueryBuilder {
        entries ?: return this
        existingEntries[table.longName] = entries
                .filterNotNull()
                .associateTo(mutableMapOf()) { it.id!! to it }
        return this@ComplexOrmQueryBuilder
    }

    inline fun <reified T : ComplexOrmTable> get() = get(T::class)

    inline fun <reified T : ComplexOrmTable> getSequence(): Sequence<T> = get(T::class).asSequence()

    open fun <T : ComplexOrmTable> get(table: KClass<T>): List<T> {
        val readTableInfo =
            ReadTableInfo(restrictions, existingEntries, complexOrmTableInfo)
        return complexOrmReader.read(table, readTableInfo)
    }

    override fun toString() = restrictions.values
            .joinToString(" AND ").replace("$$.", "")

    fun addRestriction(table: KClass<out ComplexOrmTable>, restriction: String): ComplexOrmQueryBuilder {
        restrictions[table.longName] = if (table.longName !in restrictions) restriction
        else "${restrictions[table.longName]} AND $restriction"
        return this
    }

    inline fun <reified T : ComplexOrmTable> get(id: IdType?): T? = get(T::class, id)
    fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.get(table: KClass<T>, id: IdType?): T? {
        id ?: return null
        val tableClassName = table.longName
        this@ComplexOrmQueryBuilder.restrictions[tableClassName] = if (tableClassName !in restrictions) "$$.id = ${id.asSql}"
        else "${restrictions[tableClassName]} AND $$.id = ${id.asSql}"
        val readTableInfo =
            ReadTableInfo(restrictions, existingEntries, complexOrmTableInfo)
        return complexOrmReader.read(table, readTableInfo).firstOrNull()
    }
}
