package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmReverseConnectedColumn
import com.github.silasgermany.complexormapi.ComplexOrmReverseJoinColumn
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import com.github.silasgermany.complexormprocessor.models.TableInfo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class FileCreatorTableInfo(private val tablesInfo: MutableMap<String, TableInfo>) {

    val tableInfoList = tablesInfo.toList()

    fun createConstructors(): PropertySpec {
        val constructors = tableInfoList.joinToString(",") { (className, _) ->
            "\n\"$className\" to { it: Map<String, Any?> -> $className(it as MutableMap<String, Any?>)}"
        }
        return PropertySpec.builder("constructors", constructorsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($constructors)")
            .build()
    }

    fun createNormalColumnsInfo(): PropertySpec {
        val normalColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            val writtenColumns = mutableSetOf<String>()
            tableInfo.columns.mapNotNull { column ->
                when (column.type.type) {
                    ComplexOrmTypes.ComplexOrmTable, ComplexOrmTypes.ComplexOrmTables -> null
                    else -> {
                        if (!writtenColumns.add(column.columnName)) null
                        else "\n\t\t\"${column.columnName}\" to ComplexOrmTypes.${column.type.type}"
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

    fun createConnectedColumnsInfo(): PropertySpec {
        val connectedColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            tableInfo.columns.mapNotNull { column ->
                when (column.type.type) {
                    ComplexOrmTypes.ComplexOrmTable -> {
                        "\n\t\t\"${column.columnName}\" to \"${column.type.referenceTable!!}\""
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
            tableInfo.columns.mapNotNull { column ->
                val reverseJoinColumn = column.getAnnotationValue(ComplexOrmReverseJoinColumn::class)
                val reverseConnectedColumn = column.getAnnotationValue(ComplexOrmReverseConnectedColumn::class)
                if (reverseJoinColumn != null || reverseConnectedColumn != null) null
                else when (column.type.type) {
                    ComplexOrmTypes.ComplexOrmTables -> {
                        "\n\t\t\"${column.columnName}\" to\n\t\t\t\"${column.type.referenceTable!!}\""
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
            tableInfo.columns.mapNotNull { column ->
                val reverseJoinColumn = column.getAnnotationValue(ComplexOrmReverseJoinColumn::class)
                if (reverseJoinColumn == null) null
                else when (column.type.type) {
                    ComplexOrmTypes.ComplexOrmTables -> {
                        val reverseColumn = tablesInfo.getValue(column.type.referenceTable!!).columns
                            .find { reverseJoinColumn in arrayOf(it.columnName, it.name) }?.columnName
                            ?: throw IllegalArgumentException("Couldn't find column $reverseJoinColumn in table ${column.type.referenceTable}")
                        "\n\t\t\"${column.columnName}\" to" +
                                "\n\t\t\t(\"${column.type.referenceTable}\" to \"$reverseColumn\")"
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
            tableInfo.columns.mapNotNull { column ->
                val reverseConnectedColumn = column.getAnnotationValue(ComplexOrmReverseConnectedColumn::class)
                if (reverseConnectedColumn == null) null
                else when (column.type.type) {
                    ComplexOrmTypes.ComplexOrmTables -> {
                        val reverseColumn = if (reverseConnectedColumn == Unit) tableInfo.tableName
                        else tablesInfo.getValue(column.type.referenceTable!!).columns
                            .find { it.name == reverseConnectedColumn }?.columnName ?: reverseConnectedColumn
                        "\n\t\t\"${column.columnName}\" to" +
                                "\n\t\t\t(\"${column.type.referenceTable}\" to\n\t\t\t\"$reverseColumn\")"
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

    private val stringType get() = String::class.asTypeName()
    private val typesMapType get() = Map::class.asClassName().parameterizedBy(stringType, ComplexOrmTypes::class.asTypeName())
    private val nullablePairType get() = Pair::class.asClassName().parameterizedBy(stringType, stringType.copy(true))
    private val nullablePairMapType get() = Map::class.asClassName().parameterizedBy(stringType, nullablePairType)
    private val stringMapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType)

    private val normalColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, typesMapType)
    private val normalForeignColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, stringMapType)
    private val reverseForeignColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, nullablePairMapType)

    private val nullableAnyType get() = Any::class.asTypeName().copy(true)
    private val nullableAnyMapType get() = MutableMap::class.asClassName().parameterizedBy(stringType, nullableAnyType)
    private val constructorType get() = LambdaTypeName.get(null, nullableAnyMapType, returnType = ComplexOrmTable::class.asTypeName())
    private val constructorsType get() = Map::class.asClassName().parameterizedBy(stringType, constructorType)
}