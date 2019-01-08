package com.github.silasgermany.complexorm

import android.database.sqlite.SQLiteDatabase

class QueryBuilder(private val database: SQLiteDatabase?): SqlUtils() {
/*
    private val restrictions = mutableMapOf<String, String>()
    private val existingEntries = mutableMapOf<String, MutableMap<Int, SqlTable>>()

    inline fun <reified T: SqlTable> specialWhere(column: KProperty1<T, Any?>, selection: String,
                                                       vararg selectionArguments: Any?): QueryBuilder
            = where(T::class, column, selection, *selectionArguments)
    inline fun <reified T: SqlTable> where(column: KProperty1<T, Any?>, equals: Any?): QueryBuilder
            = where(T::class, column, null, equals)
    fun <T: SqlTable> QueryBuilder.where(table: KClass<T>, column: KProperty1<T, Any?>,
                                              selection: String?, vararg selectionArguments: Any?): QueryBuilder {
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
                        whereArgument.any { it is SqlTable } ->
                            whereArgument.joinToString { (it as? SqlTable)?.id?.toString() ?: "-1" }
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
        return this@QueryBuilder
    }

    inline fun <reified T: SqlTable> alreadyLoaded(entries: Collection<T>)
            = alreadyLoaded(T::class, entries)
    fun <T: SqlTable> QueryBuilder.alreadyLoaded(table: KClass<T>, entries: Collection<T>): QueryBuilder {
        existingEntries[table.tableName.toLowerCase()] = entries
            .associateTo(mutableMapOf()) { it.id!! to it }
        return this@QueryBuilder
    }

    fun <T: SqlTable> QueryBuilder.get(table: KClass<T>): List<T> {
        return SqlReader(table)().run{ SqlReader(table)(table, this@QueryBuilder.restrictions, existingEntries) }
    }
    inline fun <reified T: SqlTable> get(): List<T>
            = get(T::class)

    fun <T: SqlTable> QueryBuilder.get(table: KClass<T>, id: Int?): T? {
        id ?: return null
        val tableName = table.tableName.toLowerCase()
        this@QueryBuilder.restrictions[tableName] = if (restrictions[tableName] == null) "$tableName.id = $id"
        else "${restrictions[tableName]} AND $tableName.id = $id"
        return SqlReader(table).run{ database!!.(table, restrictions, existingEntries) }
            .getOrNull(0)
    }
    inline fun <reified T: SqlTable> get(id: Int?): T? = get(T::class, id)
*/
}