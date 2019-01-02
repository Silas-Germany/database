package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexorm.SqlIgnore
import com.github.silasgermany.complexorm.SqlReverseConnectedColumn
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
        val normalColumnInfo = internTables.toList().joinToString(",") { (file, tables) ->
            "\n\"${file.simpleName}\" to mapOf(" + tables.joinToString(",", postfix = ")"){ table ->
                "\n\t\"${table.sql}\" to mapOf(" + table.enclosedElements.mapNotNull { column ->
                    val columnName = column.sql.removePrefix("get_")
                    val rootAnnotations = rootAnnotations[table.sql]?.find { "$it".startsWith(columnName) }
                    val annotations = internAnnotations[table]?.find { "$it".startsWith(columnName) }
                    if (!column.simpleName.startsWith("get")) null
                    else if (annotations?.getAnnotation(SqlIgnore::class.java) != null &&
                        rootAnnotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (annotations?.getAnnotation(SqlReverseConnectedColumn::class.java) != null ||
                        rootAnnotations?.getAnnotation(SqlReverseConnectedColumn::class.java) != null) null
                    else if (column.type == SqlUtils.SqlTypes.SqlTables ||
                        column.type == SqlUtils.SqlTypes.SqlTables) null
                    else "\n\t\t\"$columnName\" to \"${column.asType().toString().removePrefix("()")}\""
                }.joinToString(",", postfix = ")")
            }
        }
        return PropertySpec.builder("normalColumns", interfaceColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($normalColumnInfo)")
            .build()
    }

    fun createJoinColumnsInfo(): PropertySpec {
        val joinColumnInfo = internTables.toList().mapNotNull { (file, tables) ->
            val fileInfo = tables.mapNotNull { table ->
                val tableInfo = table.enclosedElements.mapNotNull { column ->
                    val columnName = column.sql.removePrefix("get_")
                    val rootAnnotations = rootAnnotations[table.sql]?.find { "$it".startsWith(columnName) }
                    val annotations = internAnnotations[table]?.find { "$it".startsWith(columnName) }
                    if (!column.simpleName.startsWith("get")) null
                    else if (annotations?.getAnnotation(SqlIgnore::class.java) != null &&
                        rootAnnotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (annotations?.getAnnotation(SqlReverseConnectedColumn::class.java) != null ||
                        rootAnnotations?.getAnnotation(SqlReverseConnectedColumn::class.java) != null) null
                    else if (column.type != SqlUtils.SqlTypes.SqlTables) null
                    else {
                        val foreignTableName = column.asType().toString()
                            .run { substring(lastIndexOf('.') + 1) }
                            .underScore.removeSuffix(">")
                        "\n\t\t\"$columnName\" to \"$foreignTableName\""
                    }
                }
                if (tableInfo.isEmpty()) null
                else "\n\t\"${table.sql}\" to mapOf(" + tableInfo.joinToString(",", postfix = ")")
            }
            if (fileInfo.isEmpty()) null
            else "\n\"${file.simpleName}\" to mapOf(" + fileInfo.joinToString(",", postfix = ")")
        }.joinToString(",")
        return PropertySpec.builder("joinColumns", interfaceColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($joinColumnInfo)")
            .build()
    }

    fun createConnectedColumnsInfo(): PropertySpec {
        val joinColumnInfo = internTables.toList().mapNotNull { (file, tables) ->
            val fileInfo = tables.mapNotNull { table ->
                val tableInfo = table.enclosedElements.mapNotNull { column ->
                    val columnName = column.sql.removePrefix("get_")
                    val rootAnnotations = rootAnnotations[table.sql]?.find { "$it".startsWith(columnName) }
                    val annotations = internAnnotations[table]?.find { "$it".startsWith(columnName) }
                    if (!column.simpleName.startsWith("get")) null
                    else if (annotations?.getAnnotation(SqlIgnore::class.java) != null &&
                        rootAnnotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (column.type != SqlUtils.SqlTypes.SqlTable) null
                    else {
                        val foreignTableName = column.asType().toString()
                            .run { substring(lastIndexOf('.') + 1) }
                            .removeSuffix(">").underScore
                            .takeUnless { it == columnName }?.let { "\"$it\"" } ?: "null"
                        "\n\t\t\"$columnName\" to $foreignTableName"
                    }
                }
                if (tableInfo.isEmpty()) null
                else "\n\t\"${table.sql}\" to mapOf(" + tableInfo.joinToString(",", postfix = ")")
            }
            if (fileInfo.isEmpty()) null
            else "\n\"${file.simpleName}\" to mapOf(" + fileInfo.joinToString(",", postfix = ")")
        }.joinToString(",")
        return PropertySpec.builder("connectedColumns", interfaceNullableColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($joinColumnInfo)")
            .build()
    }

    fun createReverseConnectedColumnsInfo(): PropertySpec {
        val joinColumnInfo = internTables.toList().mapNotNull { (file, tables) ->
            val fileInfo = tables.mapNotNull { table ->
                val tableInfo = table.enclosedElements.mapNotNull { column ->
                    val columnName = column.sql.removePrefix("get_")
                    val rootAnnotations = rootAnnotations[table.sql]?.find { "$it".startsWith(columnName) }
                    val annotations = internAnnotations[table]?.find { "$it".startsWith(columnName) }
                    if (!column.simpleName.startsWith("get")) null
                    else if (annotations?.getAnnotation(SqlIgnore::class.java) != null &&
                        rootAnnotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (annotations?.getAnnotation(SqlReverseConnectedColumn::class.java) == null &&
                        rootAnnotations?.getAnnotation(SqlReverseConnectedColumn::class.java) == null) null
                    else if (column.type != SqlUtils.SqlTypes.SqlTables) throw IllegalArgumentException("Reverse connected tables have to be of type List<*>")
                    else {
                        val foreignTableName = column.asType().toString()
                            .run { substring(lastIndexOf('.') + 1) }
                            .removeSuffix(">").underScore
                        val reverseColumnAnnotation = annotations?.getAnnotation(SqlReverseConnectedColumn::class.java)
                            ?: rootAnnotations?.getAnnotation(SqlReverseConnectedColumn::class.java)
                        val reverseColumn = reverseColumnAnnotation!!.connectedColumn
                            .takeUnless { it.isEmpty() }?.let { "\"$it\"" } ?: "null"
                        "\n\t\t\"$columnName\" to (\"$foreignTableName\" to $reverseColumn)"
                    }
                }
                if (tableInfo.isEmpty()) null
                else "\n\t\"${table.sql}\" to mapOf(" + tableInfo.joinToString(",", postfix = ")")
            }
            if (fileInfo.isEmpty()) null
            else "\n\"${file.simpleName}\" to mapOf(" + fileInfo.joinToString(",", postfix = ")")
        }.joinToString(",")
        return PropertySpec.builder("reverseConnectedColumns", interfaceNullableComplexColumnsType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf($joinColumnInfo)")
            .build()
    }
}