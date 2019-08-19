package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.models.ComplexOrmDatabase
import com.github.silasgermany.complexorm.models.ReadTableInfo
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.IdType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@Suppress("UNUSED")
class ComplexOrm(databasePath: String) {
    private val database = ComplexOrmDatabase(databasePath)

    private val complexOrmSchema by lazy { databaseSchema }
    private val complexOrmTableInfo by lazy { tableInfo }

    val complexOrmReader = ComplexOrmReader(database, complexOrmTableInfo)
    val complexOrmInitializer =
        ComplexOrmInitializer(database, complexOrmSchema, complexOrmTableInfo)
    val complexOrmWriter = ComplexOrmWriter(database, complexOrmTableInfo)

    inline fun <reified T : ComplexOrmTable> read(readTableInfo: ReadTableInfo) =
            complexOrmReader.read<T>(readTableInfo)
    fun <T : ComplexOrmTable> read(table: KClass<T>, readTableInfo: ReadTableInfo) =
            complexOrmReader.read(table, readTableInfo)

    inline fun <reified T: ComplexOrmTable, reified R: Any>getOneColumn(column: KProperty1<T, R?>, id: IdType) =
            complexOrmReader.complexOrmQuery.getOneColumn(T::class, column, id, R::class)

    inline fun <reified T: ComplexOrmTable, reified R: Any>saveOneColumn(column: KProperty1<T, R?>, entry: ComplexOrmTable, value: R?) =
        complexOrmWriter.saveOneColumn(T::class, column, entry.id, value)
    inline fun <reified T: ComplexOrmTable, reified R: Any>saveOneColumn(column: KProperty1<T, R?>, id: IdType?, value: R?) =
            complexOrmWriter.saveOneColumn(T::class, column, id, value)

    fun <T: ComplexOrmTable> changeId(table: KClass<T>, oldId: IdType, newId: IdType) =
            complexOrmWriter.changeId(table, oldId, newId)

    inline fun <reified T: ComplexOrmTable>createTableIfNotExists() =
            complexOrmInitializer.createTableIfNotExists<T>()
    fun <T: ComplexOrmTable>createTableIfNotExists(table: KClass<T>) =
            complexOrmInitializer.createTableIfNotExists(table)
    inline fun <reified T: ComplexOrmTable>dropTableIfExists() =
            complexOrmInitializer.dropTableIfExists<T>()
    fun <T: ComplexOrmTable>dropTableIfExists(table: KClass<T>) =
            complexOrmInitializer.dropTableIfExists(table)
    inline fun <reified T: ComplexOrmTable>replaceTable() =
            complexOrmInitializer.replaceTable<T>()
    fun <T: ComplexOrmTable>replaceTable(table: KClass<T>) =
            complexOrmInitializer.replaceTable(table)
    fun recreateAllTables() = complexOrmInitializer.recreateAllTables()
    fun createAllTables() = complexOrmInitializer.createAllTables()

    fun save(table: ComplexOrmTable, writeDeep: Boolean = true) =
            complexOrmWriter.save(table, writeDeep)

    private val tables get() = complexOrmSchema.tables
    val allTables = tables.values.toList()
    val allTableNames = tables.keys.toList()

    @Suppress("MemberVisibilityCanBePrivate")
    val KClass<out ComplexOrmTable>.name: String get() = longName

    fun getNormalColumnNames(table: KClass<out ComplexOrmTable>) =
        complexOrmTableInfo.normalColumns[table.name]?.keys?.toList() ?: emptyList()
    fun getNormalColumns(table: KClass<out ComplexOrmTable>) =
        complexOrmTableInfo.normalColumns[table.name]?.map { it.key to it.value.asType } ?: emptyList()
    fun getConnectedColumnNames(table: KClass<out ComplexOrmTable>) =
        complexOrmTableInfo.connectedColumns[table.name]?.keys?.map { "${it}_id" } ?: emptyList()
    fun getJoinColumnNames(table: KClass<out ComplexOrmTable>) =
            complexOrmTableInfo.joinColumns[table.name]?.keys?.toList() ?: emptyList()
    @Suppress("MemberVisibilityCanBePrivate")
    fun getJoinTableNames(table: KClass<out ComplexOrmTable>): List<String> {
        val tableName = table.tableName
        return getJoinTableNames(table).map { "${tableName}_$it" }
    }

    fun getRootTableClass(tableName: String): KClass<out ComplexOrmTable> =
        complexOrmSchema.tables.getValue(tableName.toSql())

    inline fun <reified T: ComplexOrmTable, R> fullColumnName(column: KProperty1<T, R>): String =
        fullColumnName(T::class, column)
    fun <T: ComplexOrmTable, R> fullColumnName(table: KClass<T>, column: KProperty1<T, R>): String =
            table.tableName + "." + columnName(table, column)

    fun <T: ComplexOrmTable> tableName(table: KClass<T>): String = table.tableName
    inline fun <reified T: ComplexOrmTable, R> columnName(column: KProperty1<T, R>) =
        columnName(T::class, column)
    fun <T: ComplexOrmTable, R> columnName(table: KClass<out ComplexOrmTable>, column: KProperty1<T, R>): String {
        var columnName = column.name.toSql()
        if (complexOrmTableInfo.connectedColumns[table.longName]
                ?.contains(columnName) == true) columnName += "_id"
        return columnName
    }

    val query get() = ComplexOrmQueryBuilder(complexOrmReader, complexOrmTableInfo)

    var version: Int
    get() = complexOrmInitializer.version
    set(value) { complexOrmInitializer.version = value }
    fun queryForEach(sql: String, f: (ComplexOrmCursor) -> Unit) = complexOrmReader.queryForEach(sql, f)
    fun <T>queryMap(sql: String, f: (ComplexOrmCursor) -> T) = complexOrmReader.queryMap(sql, f)
    fun execSQL(sql: String) = complexOrmWriter.execSQL(sql)
    inline fun <T>doInTransaction(f: () -> T) = complexOrmWriter.doInTransaction(f)
}