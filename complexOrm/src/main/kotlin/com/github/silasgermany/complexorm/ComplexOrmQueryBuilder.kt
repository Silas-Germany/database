package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ComplexOrmQueryBuilder(private val database: ComplexOrmDatabaseInterface) {

    private val complexOrmTableInfo = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface
    private val normalColumns = complexOrmTableInfo.normalColumns
    private val connectedColumns = complexOrmTableInfo.connectedColumns
    private val KClass<out ComplexOrmTable>.tableName get() = complexOrmTableInfo.basicTableInfo.getValue(qualifiedName!!).first

    private val restrictions = mutableMapOf<String, String>()
    private val existingEntries = mutableMapOf<String, MutableMap<Long, ComplexOrmTable>>()

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
        val tableName = table.tableName
        val columnName = column.name.replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
        var fullColumnName = "$tableName.$columnName"
        if (connectedColumns[table.qualifiedName!!]?.contains(columnName) == true) fullColumnName += "_id"
        else if (normalColumns[table.qualifiedName!!]?.contains(columnName) != true)
            throw java.lang.IllegalArgumentException("Can't create restriction for join columns (column ${column.name} from ${table.qualifiedName}). Please restrict the target table instead")
        var where = (selection?.let { "($it)" } ?: "?? = ?")
        if (selectionArguments.isEmpty()) where = where.replace("??", fullColumnName)
        selectionArguments.forEach { whereArgument ->
            val transformedWhereArgument = when (whereArgument) {
                is Boolean -> if (whereArgument) "1" else "0"
                is String -> {
                    if ('%' in whereArgument)
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
                is ComplexOrmTable -> "${whereArgument.id}"
                null -> {
                    where = where
                        .replace(" != ?", " IS NOT ?")
                        .replace(" = ?", " IS ?")
                    "NULL"
                }
                else -> throw IllegalArgumentException("Can't create restriction with type of $whereArgument (${whereArgument::class})")
            }
            where = where.replace("??", fullColumnName)
                .replaceFirst("?", transformedWhereArgument)
        }
        restrictions[table.qualifiedName!!] = if (table.qualifiedName!! !in restrictions) where
        else "${restrictions[table.qualifiedName!!]} AND $where"
        return this@ComplexOrmQueryBuilder
    }

    inline fun <reified T : ComplexOrmTable> alreadyLoaded(entries: Collection<T>) = alreadyLoaded(T::class, entries)
    fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.alreadyLoaded(table: KClass<T>, entries: Collection<T>): ComplexOrmQueryBuilder {
        existingEntries[table.tableName.toLowerCase()] = entries
            .associateTo(mutableMapOf()) { it.id!! to it }
        return this@ComplexOrmQueryBuilder
    }

    inline fun <reified T : ComplexOrmTable> get(): List<T> = get(T::class)

    fun <T : ComplexOrmTable> get(table: KClass<T>): List<T> {
        System.out.println("Exec SQL: $restrictions")
        val readTableInfo = ReadTableInfo(restrictions, existingEntries)
        return ComplexOrmReader(database).read(table, readTableInfo)
            .also { readTableInfo.print() }
    }

    inline fun <reified T : ComplexOrmTable> get(id: Int?): T? = get(T::class, id)
    fun <T : ComplexOrmTable> ComplexOrmQueryBuilder.get(table: KClass<T>, id: Int?): T? {
        id ?: return null
        val tableName = table.tableName.toLowerCase()
        this@ComplexOrmQueryBuilder.restrictions[tableName] = if (tableName in restrictions) "$tableName._id = $id"
        else "${restrictions[tableName]} AND $tableName._id = $id"
        System.out.println("Exec SQL: $restrictions")
        val readTableInfo = ReadTableInfo(restrictions, existingEntries)
        return ComplexOrmReader(database).read(table, readTableInfo).getOrNull(0)
    }
}
