package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import com.github.silasgermany.complexormprocessor.models.TableInfo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class FileCreatorTableInfo(private val tableInfo: MutableMap<String, TableInfo>) {

    val tableInfoList = tableInfo.toList()

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
            tableInfo.columns.mapNotNull columns@{ column ->
                when (column.type.type) {
                    ComplexOrmTypes.ComplexOrmTable, ComplexOrmTypes.ComplexOrmTables -> null
                    else -> "\n\t\t\"${column.columnName}\" to ComplexOrmTypes.${column.type.type}"
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("normalColumns", normalColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($normalColumnInfo)")
            .build()
    }

    fun createJoinColumnsInfo(): PropertySpec {
        val joinColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            tableInfo.columns.mapNotNull columns@{ column ->
                when (column.type.type) {
                    ComplexOrmTypes.ComplexOrmTables -> {
                        "\n\t\t\"${column.columnName}\" to \n\t\t\t\"${column.type.referenceTable!!}\""
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("joinColumns", joinColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($joinColumnInfo)")
            .build()
    }

    fun createConnectedColumnsInfo(): PropertySpec {
        val connectedColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            tableInfo.columns.mapNotNull columns@{ column ->
                when (column.type.type) {
                    ComplexOrmTypes.ComplexOrmTable -> {
                        "\n\t\t\"${column.columnName}\" to \"${column.type.referenceTable!!}\""
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("connectedColumns", connectedColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($connectedColumnInfo)")
            .build()
    }

    fun createReverseConnectedColumnsInfo(): PropertySpec {
        val reverseConnectedColumnInfo = tableInfoList.mapNotNull { (className, tableInfo) ->
            tableInfo.columns.mapNotNull columns@{ column ->
                when (column.type.type) {
                    ComplexOrmTypes.ComplexOrmTables -> {
                        /*
                        val foreignTableName = column.asType().toString()
                            .run { substring(lastIndexOf('.') + 1) }
                            .removeSuffix(">").sql
                        val reverseColumnAnnotation = annotations?.getAnnotation(ComplexOrmReverseConnectedColumn::class.java)
                            ?: rootAnnotations?.getAnnotation(ComplexOrmReverseConnectedColumn::class.java)
                        val reverseColumn = reverseColumnAnnotation!!.connectedColumn
                            .takeUnless { it.isEmpty() }?.let { "\"$it\"" } ?: "null"
                            // */
                        val reverseColumn = ""
                        "\n\t\t\"${column.columnName}\" to \n\t\t\t(\"${column.type.referenceTable}\" to \"$reverseColumn\")"
                    }
                    else -> null
                }
            }.takeUnless { it.isEmpty() }
                ?.run { "\n\"$className\" to mapOf(" + joinToString(",", postfix = "\n\t)") }
        }.joinToString(",")
        return PropertySpec.builder("reverseConnectedColumns", reverseConnectedColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($reverseConnectedColumnInfo)")
            .build()
    }

    private val stringType get() = String::class.asTypeName()
    private val nullablePairType get() = Pair::class.asClassName().parameterizedBy(stringType, stringType.copy(true))
    private val stringMapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType)
    private val typesMapType get() = Map::class.asClassName().parameterizedBy(stringType, ComplexOrmTypes::class.asTypeName())
    private val nullableStringMapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType.copy(true))
    private val nullablePairMapType get() = Map::class.asClassName().parameterizedBy(stringType, nullablePairType)

    private val normalColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, typesMapType)
    private val joinColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, stringMapType)
    private val connectedColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, nullableStringMapType)
    private val reverseConnectedColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, nullablePairMapType)

    private val nullableAnyType get() = Any::class.asTypeName().copy(true)
    private val nullableAnyMapType get() = MutableMap::class.asClassName().parameterizedBy(stringType, nullableAnyType)
    private val constructorType get() = LambdaTypeName.get(null, nullableAnyMapType, returnType = ComplexOrmTable::class.asTypeName())
    private val constructorsType get() = Map::class.asClassName().parameterizedBy(stringType, constructorType)
}