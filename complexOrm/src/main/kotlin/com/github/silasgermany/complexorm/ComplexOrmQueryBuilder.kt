package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ComplexOrmQueryBuilder {

    private val complexOrmTableInfo = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface

    private val restrictions = mutableMapOf<String, String>()
    private val existingEntries = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()

    inline fun <reified T : ComplexOrmTable> specialWhere(
        column: KProperty1<T, Any?>, selection: String,
        vararg selectionArguments: Any?
    ): ComplexOrmQueryBuilder = where(T::class, column, selection, *selectionArguments)

    inline fun <reified T : ComplexOrmTable> where(column: KProperty1<T, Any?>, equals: Any?): ComplexOrmQueryBuilder =
        where(T::class, column, null, equals)

    private val KProperty1<out ComplexOrmTable, Any?>.columnName get() = this.name
    private val KClass<out ComplexOrmTable>.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(qualifiedName!!).first

    fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.where(
        table: KClass<T>, column: KProperty1<T, Any?>,
        selection: String?, vararg selectionArguments: Any?
    ): ComplexOrmQueryBuilder {
        val tableName = table.tableName
        val columnName = "$tableName.${column.columnName}"
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
        return this@ComplexOrmQueryBuilder
    }

    inline fun <reified T : ComplexOrmTable> alreadyLoaded(entries: Collection<T>) = alreadyLoaded(T::class, entries)
    fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.alreadyLoaded(table: KClass<T>, entries: Collection<T>): ComplexOrmQueryBuilder {
        existingEntries[table.tableName.toLowerCase()] = entries
            .associateTo(mutableMapOf()) { it.id!! to it }
        return this@ComplexOrmQueryBuilder
    }

    fun <T : ComplexOrmTable> get(table: KClass<T>): List<T> {
        return ComplexOrmReader(1 as ComplexOrmDatabaseInterface, table) as List<T>
    }

    inline fun <reified T : ComplexOrmTable> get(): List<T> = get(T::class)

    fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.get(table: KClass<T>, id: Int?): T? {
        id ?: return null
        val tableName = table.tableName.toLowerCase()
        this@ComplexOrmQueryBuilder.restrictions[tableName] = if (restrictions[tableName] == null) "$tableName._id = $id"
        else "${restrictions[tableName]} AND $tableName._id = $id"
        return ComplexOrmReader(1 as ComplexOrmDatabaseInterface, table) as T?
    }
}
