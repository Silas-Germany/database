package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.SqlIgnore
import com.github.silasgermany.complexormapi.SqlReverseConnectedColumn
import com.github.silasgermany.complexormapi.SqlTypes.SqlTable
import com.github.silasgermany.complexormapi.SqlTypes.SqlTables
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

interface ProcessNormalTables: SqlUtils {

    fun createConstructors(): PropertySpec {
        val constructors = internTables.toList().joinToString(",") { (file, tables) ->
            "\n\"${file.simpleName}\" to mapOf(" + tables.joinToString(",", postfix = ")"){ table ->
                "\n\t\"${table.sql}\" to { it: Map<String, Any?> -> $table(it as MutableMap<String, Any?>)}"
            }
        }
        return PropertySpec.builder("constructors", interfaceConstructorsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($constructors)")
            .build()
    }

    fun createNormalColumnsInfo(): PropertySpec {
        val normalColumnInfo = internTables.toList().mapNotNull { (file, tables) ->
            tables.mapNotNull{ table ->
                table.enclosedElements.mapNotNull columns@ { column ->
                    if (!column.simpleName.startsWith("get")) return@columns null
                    if (column.asType().toString().startsWith("()kotlin.jvm.functions.Function")) return@columns null
                    val columnName = column.sql.removePrefix("get_")
                    val rootAnnotations = rootAnnotations[table.sql]?.find { "$it".startsWith(columnName) }
                    val annotations = internAnnotations[table]?.find { "$it".startsWith(columnName) }
                    if (!rootTables.any { table -> table.enclosedElements.any { it.sql == column.sql } }) null
                    else if (annotations?.getAnnotation(SqlIgnore::class.java) != null ||
                        rootAnnotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (annotations?.getAnnotation(SqlReverseConnectedColumn::class.java) != null ||
                        rootAnnotations?.getAnnotation(SqlReverseConnectedColumn::class.java) != null) null
                    else if (column.type == SqlTable ||
                        column.type == SqlTables) null
                    else "\n\t\t\"$columnName\" to SqlTypes.${column.type}"
                }
                    .takeUnless { it.isEmpty() }
                    ?.run { "\n\t\"${table.sql}\" to mapOf(" + joinToString(",", postfix = ")") }
            }
                .takeUnless { it.isEmpty() }
                ?.run { "\n\"${file.simpleName}\" to mapOf(" + joinToString(",", postfix = ")") }
        }
            .takeUnless { it.isEmpty() }
            ?.joinToString(",")
        return PropertySpec.builder("normalColumns", interfaceColumnsType2)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($normalColumnInfo)")
            .build()
    }
    
    fun createJoinColumnsInfo(): PropertySpec {
        val joinColumnInfo = internTables.toList().mapNotNull { (file, tables) ->
            tables.mapNotNull { table ->
                table.enclosedElements.mapNotNull columns@ { column ->
                    if (!column.simpleName.startsWith("get")) return@columns null
                    if (column.asType().toString().startsWith("()kotlin.jvm.functions.Function")) return@columns null
                    val columnName = column.sql.removePrefix("get_")
                    val rootAnnotations = rootAnnotations[table.sql]?.find { "$it".startsWith(columnName) }
                    val annotations = internAnnotations[table]?.find { "$it".startsWith(columnName) }
                    if (!rootTables.any { table -> table.enclosedElements.any { it.sql == column.sql } }) null
                    else if (annotations?.getAnnotation(SqlIgnore::class.java) != null ||
                        rootAnnotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (annotations?.getAnnotation(SqlReverseConnectedColumn::class.java) != null ||
                        rootAnnotations?.getAnnotation(SqlReverseConnectedColumn::class.java) != null) null
                    else if (column.type != SqlTables) null
                    else {
                        val foreignTableName = column.asType().toString()
                            .run { substring(lastIndexOf('.') + 1) }
                            .sql.removeSuffix(">")
                        "\n\t\t\"$columnName\" to \"$foreignTableName\""
                    }
                }
                    .takeUnless { it.isEmpty() }
                    ?.run { "\n\t\"${table.sql}\" to mapOf(" + joinToString(",", postfix = ")") }
            }
                .takeUnless { it.isEmpty() }
                ?.run { "\n\"${file.simpleName}\" to mapOf(" + joinToString(",", postfix = ")") }
        }.joinToString(",")
        return PropertySpec.builder("joinColumns", interfaceColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($joinColumnInfo)")
            .build()
    }

    fun createConnectedColumnsInfo(): PropertySpec {
        var all = ""
        val joinColumnInfo = internTables.toList().mapNotNull { (file, tables) ->
            tables.mapNotNull { table ->
                table.enclosedElements.mapNotNull columns@ { column ->
                    all += "$column"
                    if (!column.simpleName.startsWith("get")) return@columns null
                    if (column.asType().toString().startsWith("()kotlin.jvm.functions.Function")) return@columns null
                    val columnName = column.sql.removePrefix("get_")
                    val rootAnnotations = rootAnnotations[table.sql]?.find { "$it".startsWith(columnName) }
                    val annotations = internAnnotations[table]?.find { "$it".startsWith(columnName) }
                    if (!rootTables.any { table -> table.enclosedElements.any { it.sql == column.sql } }) null
                    else if (annotations?.getAnnotation(SqlIgnore::class.java) != null ||
                        rootAnnotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (column.type != SqlTable) null
                    else {
                        val foreignTableName = column.asType().toString()
                            .run { substring(lastIndexOf('.') + 1) }
                            .removeSuffix(">").sql
                            .takeUnless { it == columnName }?.let { "\"$it\"" } ?: "null"
                        "\n\t\t\"$columnName\" to $foreignTableName"
                    }
                }
                    .takeUnless { it.isEmpty() }
                    ?.run { "\n\t\"${table.sql}\" to mapOf(" + joinToString(",", postfix = ")") }
            }
                .takeUnless { it.isEmpty() }
                ?.run { "\n\"${file.simpleName}\" to mapOf(" + joinToString(",", postfix = ")") }
        }.joinToString(",")
        return PropertySpec.builder("connectedColumns", interfaceNullableColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($joinColumnInfo)")
            .build()
    }

    fun createReverseConnectedColumnsInfo(): PropertySpec {
        val joinColumnInfo = internTables.toList().mapNotNull { (file, tables) ->
            tables.mapNotNull { table ->
                table.enclosedElements.mapNotNull columns@ { column ->
                    if (!column.simpleName.startsWith("get")) return@columns null
                    if (column.asType().toString().startsWith("()kotlin.jvm.functions.Function:")) return@columns null
                    val columnName = column.sql.removePrefix("get_")
                    val rootAnnotations = rootAnnotations[table.sql]?.find { "$it".startsWith(columnName) }
                    val annotations = internAnnotations[table]?.find { "$it".sql.startsWith(columnName) }
                    if (annotations?.getAnnotation(SqlIgnore::class.java) != null ||
                        rootAnnotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (annotations?.getAnnotation(SqlReverseConnectedColumn::class.java) == null &&
                        rootAnnotations?.getAnnotation(SqlReverseConnectedColumn::class.java) == null) null
                    else if (column.type != SqlTables) throw IllegalArgumentException("Reverse connected tables have to be of type List<*>")
                    else {
                        val foreignTableName = column.asType().toString()
                            .run { substring(lastIndexOf('.') + 1) }
                            .removeSuffix(">").sql
                        val reverseColumnAnnotation = annotations?.getAnnotation(SqlReverseConnectedColumn::class.java)
                            ?: rootAnnotations?.getAnnotation(SqlReverseConnectedColumn::class.java)
                        val reverseColumn = reverseColumnAnnotation!!.connectedColumn
                            .takeUnless { it.isEmpty() }?.let { "\"$it\"" } ?: "null"
                        "\n\t\t\"$columnName\" to (\"$foreignTableName\" to $reverseColumn)"
                    }
                }
                    .takeUnless { it.isEmpty() }
                    ?.run { "\n\t\"${table.sql}\" to mapOf(" + joinToString(",", postfix = ")") }
            }
                .takeUnless { it.isEmpty() }
                ?.run { "\n\"${file.simpleName}\" to mapOf(" + joinToString(",", postfix = ")") }
        }.joinToString(",")
        return PropertySpec.builder("reverseConnectedColumns", interfaceNullableComplexColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($joinColumnInfo)")
            .build()
    }
}