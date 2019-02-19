package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmReverseConnectedColumn
import com.github.silasgermany.complexormapi.ComplexOrmReverseJoinColumn
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import com.github.silasgermany.complexormprocessor.models.TableInfo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class FileCreatorTableInfo(private val tablesInfo: MutableMap<String, TableInfo>) {

    private val tableInfoList = tablesInfo.toList()

    fun createNormalColumnsInfo(): PropertySpec {
        val normalColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val rootTableColumnNames = (if (tableInfo.isRoot) tableInfo else getRootTableInfo(tableInfo)).columns.map { it.columnName }
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                if (column.columnName !in rootTableColumnNames)
                    throw java.lang.IllegalArgumentException("Column ${column.name} of table $className not in root table: ${getRootTableName(tableInfo)} " +
                            "(Don't delegate it with a map, if it's not a column)")
                if (!writtenColumns.add(column.columnName)) null
                else when (column.columnType.type) {
                    ComplexOrmTypes.ComplexOrmTable, ComplexOrmTypes.ComplexOrmTables -> null
                    else -> {
                        "\n\t\t\"${column.columnName}\" to ComplexOrmTypes.${column.columnType.type}"
                    }
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("normalColumns", normalColumnsType)
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
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                if (!writtenColumns.add(column.columnName)) null
                else when (column.columnType.type) {
                    ComplexOrmTypes.ComplexOrmTable -> {
                        "\n\t\t\"${column.columnName}\" to \"${column.columnType.referenceTable!!}\""
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
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                val reverseJoinColumn = column.getAnnotationValue(ComplexOrmReverseJoinColumn::class)
                val reverseConnectedColumn = column.getAnnotationValue(ComplexOrmReverseConnectedColumn::class)
                if (reverseJoinColumn != null || reverseConnectedColumn != null) null
                else if (!writtenColumns.add(column.columnName)) null
                else when (column.columnType.type) {
                    ComplexOrmTypes.ComplexOrmTables -> {
                        "\n\t\t\"${column.columnName}\" to\n\t\t\t\"${column.columnType.referenceTable!!}\""
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
                val reverseJoinColumn = column.getAnnotationValue(ComplexOrmReverseJoinColumn::class)
                if (reverseJoinColumn == null) null
                else if (!writtenColumns.add(column.columnName)) null
                else when (column.columnType.type) {
                    ComplexOrmTypes.ComplexOrmTables -> {
                        val reverseColumn = tablesInfo.getValue(column.columnType.referenceTable!!).columns
                            .find { reverseJoinColumn in arrayOf(it.columnName, it.name) }?.columnName
                            ?: throw IllegalArgumentException("Couldn't find column $reverseJoinColumn in table ${column.columnType.referenceTable}")
                        "\n\t\t\"${column.columnName}\" to" +
                                "\n\t\t\t(\"${column.columnType.referenceTable}\" to\n\t\t\t \"$reverseColumn\")"
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("reverseJoinColumns", reverseForeignColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($reverseConnectedColumnInfo)")
            .build()
    }

    fun createReverseConnectedColumnsInfo(): PropertySpec {
        val reverseConnectedColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                val reverseConnectedColumn = column.getAnnotationValue(ComplexOrmReverseConnectedColumn::class)
                if (reverseConnectedColumn == null) null
                else if (!writtenColumns.add(column.columnName)) null
                else when (column.columnType.type) {
                    ComplexOrmTypes.ComplexOrmTables -> {
                        val reverseColumn = if (reverseConnectedColumn == Unit) tableInfo.tableName
                        else tablesInfo.getValue(column.columnType.referenceTable!!).columns
                            .find { it.name == reverseConnectedColumn }?.columnName ?: reverseConnectedColumn
                        "\n\t\t\"${column.columnName}\" to" +
                                "\n\t\t\t(\"${column.columnType.referenceTable}\" to\n\t\t\t\"$reverseColumn\")"
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("reverseConnectedColumns", reverseForeignColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($reverseConnectedColumnInfo)")
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
    private val typesMapType get() = Map::class.asClassName().parameterizedBy(stringType, ComplexOrmTypes::class.asTypeName())
    private val stringMapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType)
    private val pairType get() = Pair::class.asClassName().parameterizedBy(stringType, stringType)
    private val pairMapType get() = Map::class.asClassName().parameterizedBy(stringType, pairType)

    private val normalColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, typesMapType)
    private val normalForeignColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, stringMapType)
    private val reverseForeignColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, pairMapType)

    private val nullableAnyType get() = Any::class.asTypeName().copy(true)
    private val nullableAnyMapType get() = MutableMap::class.asClassName().parameterizedBy(stringType, nullableAnyType)
    private val constructorType get() = LambdaTypeName.get(null, nullableAnyMapType, returnType = ComplexOrmTable::class.asTypeName())
    private val constructorsType get() = Map::class.asClassName().parameterizedBy(stringType, constructorType)
}