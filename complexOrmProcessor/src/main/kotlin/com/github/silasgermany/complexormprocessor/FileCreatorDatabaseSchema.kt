package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.*
import com.github.silasgermany.complexormprocessor.models.Column
import com.github.silasgermany.complexormprocessor.models.InternComplexOrmTypes
import com.github.silasgermany.complexormprocessor.models.TableInfo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmWildcard
import kotlin.reflect.KClass

class FileCreatorDatabaseSchema(tableInfo: MutableMap<String, TableInfo>) {

    private val rootTables = tableInfo.filter { it.value.isRoot }
    private val rootTablesList = rootTables.toList()

    fun createNames(): PropertySpec {
        val joinTables = mutableSetOf<String>()
        val tableNames = (rootTablesList.map { tableInfo ->
            val tableName = tableInfo.second.tableName!!
            tableInfo.second.columns.forEach { column ->
                column.getAnnotationValue(ComplexOrmIndex::class)?.let {
                    val specialTableName = "index_${tableName}_${it as? Int ?: 1}"
                    joinTables.add("\n\"$specialTableName\" to ${tableInfo.first}::class")
                }
                if (column.columnType.type == InternComplexOrmTypes.ComplexOrmTables) {
                    val specialTableName = "${tableName}_${column.columnName}"
                    joinTables.add("\n\"$specialTableName\" to ${tableInfo.first}::class")
                }
            }
            "\n\"$tableName\" to ${tableInfo.first}::class"
        } + joinTables).joinToString(",")
        return PropertySpec.builder("tables", tableClassMapType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("sortedMapOf($tableNames)")
            .build()
    }

    fun createDropTables(): PropertySpec {
        val joinTables = mutableSetOf<String>()
        val dropTableCommands = (rootTablesList.map {
            val tableName = it.second.tableName!!
            it.second.columns.forEach { column ->
                column.getAnnotationValue(ComplexOrmIndex::class)?.let {
                    joinTables.add("index_${tableName}_${it as? Int ?: 1}")
                }
                if (column.columnType.type == InternComplexOrmTypes.ComplexOrmTables)
                    joinTables.add("${tableName}_${column.columnName}")
            }
            tableName
        } + joinTables).joinToString(",") {
            "\n\"$it\" to \"\"\"DROP TABLE IF EXISTS '$it';\"\"\"" }
        return PropertySpec.builder("dropTableCommands", stringMapType)
                .addModifiers(KModifier.OVERRIDE)
                .initializer("sortedMapOf($dropTableCommands)"
                )
            .build()
    }

    fun createCreateTables(): PropertySpec {
        val relatedTables = mutableListOf<String>()
        val createTableCommands = rootTablesList.map { (_, tableInfo) ->
            val writtenColumns = mutableSetOf("id")
            val uniqueColumns = mutableMapOf<Int, MutableList<String>>()
            val indexColumns = mutableMapOf<Int, MutableList<String>>()
            val columns = arrayOf("'id' BLOB NOT NULL PRIMARY KEY") +
                    tableInfo.columns.mapNotNull { column ->
                        if (!writtenColumns.add(column.idName)) return@mapNotNull null
                        var columnExtra = ""
                        // Check whether nullable
                        if (!column.columnType.nullable) columnExtra += " NOT NULL"
                        // Check annotations
                        column.getAnnotationValue(ComplexOrmDefault::class)?.let {
                            columnExtra += defaultValue(column.columnType.type, "$it")
                        }
                        column.getAnnotationValue(ComplexOrmProperty::class)?.let {
                            columnExtra += " $it".replace(column.name, "'${tableInfo.tableName}'.'${column.columnName}'")
                        }
                        column.getAnnotationValue(ComplexOrmUnique::class)?.let {
                            columnExtra += " UNIQUE"
                        }
                        column.getAnnotationValue(ComplexOrmUniqueIndex::class)?.let {
                            uniqueColumns.getOrPut(it as? Int ?: 1) { mutableListOf() }.add("'${column.idName}'")
                        }
                        column.getAnnotationValue(ComplexOrmIndex::class)?.let {
                            indexColumns.getOrPut(it as? Int ?: 1) { mutableListOf() }.add(column.idName)
                        }
                        // Get type
                        val complexOrmType = when (column.columnType.type) {
                            InternComplexOrmTypes.DateTime,
                            InternComplexOrmTypes.String -> "TEXT"
                            InternComplexOrmTypes.Boolean,
                            InternComplexOrmTypes.Date,
                            InternComplexOrmTypes.Long,
                            InternComplexOrmTypes.Int -> "INTEGER"
                            InternComplexOrmTypes.Float -> "REAL"
                            InternComplexOrmTypes.Uuid -> "BLOB"
                            InternComplexOrmTypes.ByteArray -> "BLOB"
                            InternComplexOrmTypes.ComplexOrmTables -> {
                                relatedTables.add(createRelatedTableCommand(tableInfo.tableName!!, column))
                                return@mapNotNull null
                            }
                            InternComplexOrmTypes.ComplexOrmTable -> {
                                val referenceTable = rootTables.getValue(column.columnType.referenceTable!!)
                                val specialConnectedColumn = column.getAnnotationValue(ComplexOrmSpecialConnectedColumn::class)
                                val connectedColumn = if (specialConnectedColumn != null) {
                                    referenceTable.columns
                                            .find { specialConnectedColumn in arrayOf(it.columnName, it.name, it.idName) }!!.idName
                                } else "id"
                                val onDelete = when {
                                    column.getAnnotationValue(ComplexOrmDeleteRestrict::class) != null -> "RESTRICT"
                                    column.getAnnotationValue(ComplexOrmDeleteCascade::class) != null ||
                                            !column.columnType.nullable -> "CASCADE"
                                    else -> "SET NULL"
                                }
                                return@mapNotNull "'${column.idName}' INTEGER$columnExtra " +
                                        "REFERENCES '${referenceTable.tableName!!}'('$connectedColumn') ON DELETE $onDelete"
                            }
                        }
                        "'${column.columnName}' $complexOrmType$columnExtra"
                    } + uniqueColumns.values.map { it.joinToString(",", "UNIQUE(", ")") }
            indexColumns.forEach { relatedTables.add(createIndexCommand(tableInfo.tableName!!, it.key, it.value)) }
            "\n\"${tableInfo.tableName!!}\" to \"\"\"CREATE TABLE '${tableInfo.tableName!!}'(\n${columns.joinToString(",\n")}\n);\"\"\""
        } + relatedTables
        return PropertySpec.builder("createTableCommands", stringMapType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer(CodeBlock.of("sortedMapOf(${createTableCommands.joinToString(",")})"))
            .build()
    }

    private fun defaultValue(type: InternComplexOrmTypes, defaultValue: String?): String {
        defaultValue ?: return ""
        return " DEFAULT " + when(type) {
            InternComplexOrmTypes.String -> "'$defaultValue'"
            InternComplexOrmTypes.Boolean -> when (defaultValue) {
                "false" -> "0"
                "true" -> "1"
                else -> throw java.lang.IllegalArgumentException("Use \"\${true}\" or \"\${false}\" for default values of ${type.name} (not $defaultValue)")
            }
            InternComplexOrmTypes.Date,
            InternComplexOrmTypes.DateTime,
            InternComplexOrmTypes.Uuid,
            InternComplexOrmTypes.ByteArray,
            InternComplexOrmTypes.ComplexOrmTables,
            InternComplexOrmTypes.ComplexOrmTable -> {
                throw IllegalArgumentException("Default value not allowed for ${type.name} (has $defaultValue)")
            }
            InternComplexOrmTypes.Float -> {
                try {
                    defaultValue.toFloat().toString()
                } catch (e: Throwable) {
                    throw java.lang.IllegalArgumentException("Use something like \"\${1.0}\" for default values of ${type.name} (not $defaultValue)")
                }
            }
            InternComplexOrmTypes.Long,
            InternComplexOrmTypes.Int -> {
                try {
                    defaultValue.toLong().toString()
                } catch (e: Throwable) {
                    throw java.lang.IllegalArgumentException("Use something like \"\${1}\" for default values of ${type.name} (not $defaultValue)")
                }
            }
        }
    }

    private fun createRelatedTableCommand(tableName: String, column: Column): String {
        val referenceTableName = rootTables.getValue(column.columnType.referenceTable!!).tableName!!
        val secondColumnName = if (referenceTableName == tableName) "second_$referenceTableName"
        else referenceTableName
        return "\n\"${tableName}_${column.columnName}\" to \"\"\"CREATE TABLE '${tableName}_${column.columnName}'(\n" +
                "'${tableName}_id' INTEGER NOT NULL REFERENCES '$tableName'(id) ON DELETE CASCADE,\n" +
                "'${secondColumnName}_id' INTEGER NOT NULL REFERENCES '$referenceTableName'(id) ON DELETE CASCADE,\n" +
                "PRIMARY KEY ('${tableName}_id','${secondColumnName}_id')\n" +
                ");\"\"\""
    }

    private fun createIndexCommand(tableName: String, group: Int, columns: List<String>): String {
        return "\n\"index_${tableName}_$group\" to \"\"\"CREATE INDEX 'index_${tableName}_$group' ON '$tableName'(" +
                "${columns.joinToString(",")});\"\"\""
    }

    private val stringType get() = String::class.asTypeName()
    private val stringMapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType)

    private val tableType get() = ComplexOrmTable::class.asTypeName().jvmWildcard()
    private val tableClassType get() = KClass::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(tableType))
    private val tableClassMapType get() = Map::class.asClassName().parameterizedBy(String::class.asTypeName(), tableClassType)
}