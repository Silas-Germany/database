package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmReverseConnectedColumn
import com.github.silasgermany.complexormapi.ComplexOrmReverseJoinColumn
import com.github.silasgermany.complexormapi.ComplexOrmSpecialConnectedColumn
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormprocessor.models.InternComplexOrmTypes
import com.github.silasgermany.complexormprocessor.models.TableInfo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.*

class FileCreatorTableInfo(private val tablesInfo: MutableMap<String, TableInfo>) {

    private val tableInfoList = tablesInfo.toList()

    fun createNormalColumnsInfo(): PropertySpec {
        val normalColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val rootTableColumnNames = (if (tableInfo.isRoot) tableInfo else getRootTableInfo(tableInfo)).columns.map {
                if (it.columnType.type == InternComplexOrmTypes.ComplexOrmTable)
                    it.columnName + "_id"
                else it.columnName
            }
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                if (!writtenColumns.add(column.columnName)) null
                else when (column.columnType.type) {
                    InternComplexOrmTypes.ComplexOrmTable, InternComplexOrmTypes.ComplexOrmTables -> null
                    else -> {
                        if (column.idName !in rootTableColumnNames)
                            throw java.lang.IllegalArgumentException(
                                "Column ${column.idName} of table $className not in root table: $rootTableColumnNames " +
                                        "(Don't delegate it with a map, if it's not a column)"
                            )
                        "\n\t\t\"${column.columnName}\" to \"${column.columnType.type}\""
                    }
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("normalColumns", normalForeignColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($normalColumnInfo)")
            .build()
    }

    private fun getRootTableInfo(tableInfo: TableInfo): TableInfo {
        val nextTableInfo = tablesInfo.getValue(tableInfo.superTable!!)
        return if (nextTableInfo.isRoot) nextTableInfo
        else getRootTableInfo(nextTableInfo)
    }

    fun createConnectedColumnsInfo(): PropertySpec {
        val connectedColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val rootTableColumnNames = (if (tableInfo.isRoot) tableInfo
            else getRootTableInfo(tableInfo)).columns.map { it.columnName }
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                if (!writtenColumns.add(column.columnName)) null
                else when (column.columnType.type) {
                    InternComplexOrmTypes.ComplexOrmTable -> {
                        val rootTableInfo = if (tableInfo.isRoot) tableInfo
                        else getRootTableInfo(tableInfo)
                        val rootColumn = rootTableInfo.columns.find { column.name == it.name }!!
                        val specialConnectedColumn =
                            rootColumn.getAnnotationValue(ComplexOrmSpecialConnectedColumn::class)
                        if (specialConnectedColumn != null) null
                        else {
                            if (column.columnName !in rootTableColumnNames)
                                throw java.lang.IllegalArgumentException(
                                    "Column ${column.name} of table $className not in root table: ${getRootTableName(
                                        tableInfo
                                    )} " +
                                            "(Don't delegate it with a map, if it's not a column)"
                                )
                            "\n\t\t\"${column.columnName}\" to \"${column.columnType.referenceTable!!}\""
                        }
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("connectedColumns", normalForeignColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($connectedColumnInfo)")
            .build()
    }

    fun createJoinColumnsInfo(): PropertySpec {
        val joinColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val rootTableColumnNames = (if (tableInfo.isRoot) tableInfo
            else getRootTableInfo(tableInfo)).columns.map { it.columnName }
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                when (column.columnType.type) {
                    InternComplexOrmTypes.ComplexOrmTables -> {
                        val reverseJoinColumn = column.getAnnotationValue(ComplexOrmReverseJoinColumn::class)
                        val reverseConnectedColumn = column.getAnnotationValue(ComplexOrmReverseConnectedColumn::class)
                        if (reverseJoinColumn != null || reverseConnectedColumn != null) null
                        else if (!writtenColumns.add(column.columnName)) null
                        else {
                            if (column.columnName !in rootTableColumnNames)
                                throw java.lang.IllegalArgumentException(
                                    "Column ${column.name} of table $className not in root table: ${getRootTableName(
                                        tableInfo
                                    )} " +
                                            "(Don't delegate it with a map, if it's not a column)"
                                )
                            "\n\t\t\"${column.columnName}\" to\n\t\t\t\"${column.columnType.referenceTable!!}\""
                        }
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("joinColumns", normalForeignColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($joinColumnInfo)")
            .build()
    }

    fun createReverseJoinColumnsInfo(): PropertySpec {
        val reverseConnectedColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                when (column.columnType.type) {
                    InternComplexOrmTypes.ComplexOrmTables -> {
                        val reverseJoinColumn = column.getAnnotationValue(ComplexOrmReverseJoinColumn::class)
                        if (reverseJoinColumn == null) null
                        else if (!writtenColumns.add(column.columnName)) null
                        else {
                            val connectedTableInfo = tablesInfo.getValue(column.columnType.referenceTable!!)
                            val connectedRootTableInfo = if (connectedTableInfo.isRoot) connectedTableInfo
                            else getRootTableInfo(connectedTableInfo)
                            val reverseColumn = connectedRootTableInfo.columns
                                .find { reverseJoinColumn in arrayOf(it.columnName, it.name) }?.columnName
                                ?: throw IllegalArgumentException("Couldn't find column $reverseJoinColumn in table ${connectedRootTableInfo.tableName}")
                            "\n\t\t\"${column.columnName}\" to" +
                                    "\n\t\t\t\"${column.columnType.referenceTable};\" +\n\t\t\t \"$reverseColumn\""
                        }
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("reverseJoinColumns", normalForeignColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($reverseConnectedColumnInfo)")
            .build()
    }

    fun createReverseConnectedColumnsInfo(): PropertySpec {
        val reverseConnectedColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                when (column.columnType.type) {
                    InternComplexOrmTypes.ComplexOrmTables -> {
                        val reverseConnectedColumn = column.getAnnotationValue(ComplexOrmReverseConnectedColumn::class)
                        if (reverseConnectedColumn == null) null
                        else if (!writtenColumns.add(column.columnName)) null
                        else {
                            val reverseColumn = if (reverseConnectedColumn == Unit) tableInfo.tableName
                            else tablesInfo.getValue(column.columnType.referenceTable!!).columns
                                .find { it.name == reverseConnectedColumn }?.columnName
                                ?: reverseConnectedColumn
                            "\n\t\t\"${column.columnName}\" to" +
                                    "\n\t\t\t\"${column.columnType.referenceTable};\" +\n\t\t\t\"$reverseColumn\""
                        }
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("reverseConnectedColumns", normalForeignColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($reverseConnectedColumnInfo)")
            .build()
    }

    fun createSpecialConnectedColumnsInfo(): PropertySpec {
        val connectedColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val rootTableColumnNames = (if (tableInfo.isRoot) tableInfo
            else getRootTableInfo(tableInfo)).columns.map { it.columnName }
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                if (!writtenColumns.add(column.columnName)) null
                else when (column.columnType.type) {
                    InternComplexOrmTypes.ComplexOrmTable -> {
                        if (column.columnName !in rootTableColumnNames)
                            throw java.lang.IllegalArgumentException(
                                "Column ${column.name} of table $className not in root table: ${getRootTableName(
                                    tableInfo
                                )} " +
                                        "(Don't delegate it with a map, if it's not a column)"
                            )
                        val rootTableInfo = if (tableInfo.isRoot) tableInfo
                        else getRootTableInfo(tableInfo)
                        val rootColumn = rootTableInfo.columns.find { column.name == it.name }!!
                        val specialConnectedColumn =
                            rootColumn.getAnnotationValue(ComplexOrmSpecialConnectedColumn::class)
                        if (specialConnectedColumn == null) null
                        else {
                            val connectedTableInfo = tablesInfo.getValue(column.columnType.referenceTable!!)
                            val connectedRootTableInfo = if (connectedTableInfo.isRoot) connectedTableInfo
                            else getRootTableInfo(connectedTableInfo)
                            val connectedColumn = connectedRootTableInfo.columns
                                .find { specialConnectedColumn in arrayOf(it.columnName, it.name) }?.idName
                            "\n\t\t\"${column.columnName}\" to" +
                                    "\n\t\t\t\"${column.columnType.referenceTable};\" +\n\t\t\t\"$connectedColumn\""
                        }
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("specialConnectedColumns", normalForeignColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($connectedColumnInfo)")
            .build()
    }

    fun createConstructors(): PropertySpec {
        val constructors = tableInfoList.joinToString(",") { (className, _) ->
            "\n\"$className\" to { it: Map<String, Any?> -> $className(it as MutableMap<String, Any?>)}"
        }
        return PropertySpec.builder("tableConstructors", constructorsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($constructors)")
            .build()
    }

    fun createBasicTableInfo(): PropertySpec {
        val constructors = tableInfoList.joinToString(",") { (className, tableInfo) ->
            val rootTable = if (tableInfo.isRoot) className else getRootTableName(tableInfo)
            "\n\"$className\" to (\"${tableInfo.tableName}\" to \"$rootTable\")"
        }
        return PropertySpec.builder("basicTableInfo", pairMapType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($constructors)")
            .build()
    }

    private fun getRootTableName(tableInfo: TableInfo): String {
        val nextTableInfo = tablesInfo.getValue(tableInfo.superTable!!)
        return if (nextTableInfo.isRoot) tableInfo.superTable
        else getRootTableName(nextTableInfo)
    }

    fun createColumnNames(): PropertySpec {
        val connectedColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                if (!writtenColumns.add(column.columnName)) null
                else "\n\t\t\"${column.columnName}\" to \"${column.name}\""
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("columnNames", normalForeignColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($connectedColumnInfo)")
            .build()
    }

    private val stringType get() = String::class.asTypeName()
    private val stringMapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType)
    private val pairType get() = Pair::class.asClassName().parameterizedBy(stringType, stringType)

    private val pairMapType get() = MutableMap::class.asClassName().parameterizedBy(stringType, pairType)
    private val normalForeignColumnsType
        get() = MutableMap::class.asClassName().parameterizedBy(
            stringType,
            stringMapType
        )

    private val nullableAnyType get() = Any::class.asTypeName().copy(true)
    private val nullableAnyMapType get() = MutableMap::class.asClassName().parameterizedBy(stringType, nullableAnyType)
    private val constructorType
        get() = LambdaTypeName.get(
            null,
            nullableAnyMapType,
            returnType = ComplexOrmTable::class.asTypeName()
        )
    private val constructorsType get() = MutableMap::class.asClassName().parameterizedBy(stringType, constructorType)
}