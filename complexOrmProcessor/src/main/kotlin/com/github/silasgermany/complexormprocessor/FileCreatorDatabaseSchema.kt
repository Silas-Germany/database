package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.*
import com.github.silasgermany.complexormprocessor.models.Column
import com.github.silasgermany.complexormprocessor.models.TableInfo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmWildcard
import kotlin.reflect.KClass

class FileCreatorDatabaseSchema(private val tableInfo: MutableMap<String, TableInfo>) {

    private val rootTables = tableInfo.filter { it.value.isRoot }
    private val rootTablesList = rootTables.toList()

    fun createNames(): PropertySpec {
        return PropertySpec.builder("tables", tableClassMapType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf(${rootTablesList.joinToString(",") { "\n${it.first}::class to \"${it.second.tableName}\"" }})")
            .build()
    }

    fun createDropTables(): PropertySpec {
        return PropertySpec.builder("dropTableCommands", listType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("listOf(${rootTablesList.joinToString(",") {
                "\n\"DROP TABLE IF EXISTS '${it.second.tableName}';\"" }})"
            )
            .build()
    }

    fun createCreateTables(): PropertySpec {
        val relatedTables = mutableListOf<String>()
        val createTableCommands = rootTablesList.map { (_, tableInfo) ->
            val foreignKeys = mutableListOf<String>()
            val columns = arrayOf("'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT") +
                    tableInfo.columns.mapNotNull { column ->
                        var columnExtra = ""
                        // check whether nullable
                        if (!column.type.nullable) columnExtra += " NOT NULL"
                        // check annotations
                        column.getAnnotationValue(ComplexOrmDefault::class)?.let {
                            columnExtra += defaultValue(column.type.type, "$it")
                        }
                        column.getAnnotationValue(ComplexOrmProperty::class)?.let {
                            columnExtra += " $it".replace(column.name, column.columnName)
                        }
                        column.getAnnotationValue(ComplexOrmUnique::class)?.let {
                            columnExtra += " UNIQUE"
                        }
                        // get type
                        val complexOrmType = when (column.type.type) {
                            ComplexOrmTypes.String -> "TEXT"
                            ComplexOrmTypes.Boolean,
                            ComplexOrmTypes.Date,
                            ComplexOrmTypes.LocalDate -> "NUMERIC"
                            ComplexOrmTypes.Long,
                            ComplexOrmTypes.Int -> "INTEGER"
                            ComplexOrmTypes.Float -> "REAL"
                            ComplexOrmTypes.ByteArray -> "BLOB"
                            ComplexOrmTypes.ComplexOrmTables -> {
                                relatedTables.add(createRelatedTableCommand(tableInfo.tableName!!, column))
                                return@mapNotNull null
                            }
                            ComplexOrmTypes.ComplexOrmTable -> {
                                val referenceTableName = rootTables.getValue(column.type.referenceTable!!).tableName
                                foreignKeys.add("FOREIGN KEY ('${column.columnName}_id') REFERENCES '$referenceTableName'(id)")
                                return@mapNotNull "'${column.columnName}_id' INTEGER$columnExtra"
                            }
                        }
                        "'${column.columnName}' $complexOrmType$columnExtra"
                    } + foreignKeys
            "\n\"\"\"CREATE TABLE IF NOT EXISTS '${tableInfo.tableName}'(${columns.joinToString()});\"\"\""
        } + relatedTables
        return PropertySpec.builder("createTableCommands", listType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer(CodeBlock.of("listOf(${createTableCommands.joinToString(",")})"))
            .build()
    }

    private fun defaultValue(type: ComplexOrmTypes, defaultValue: String?): String {
        defaultValue ?: return ""
        return " DEFAULT " + when(type) {
            ComplexOrmTypes.String -> "'$defaultValue'"
            ComplexOrmTypes.Boolean -> when (defaultValue) {
                "false" -> "0"
                "true" -> "1"
                else -> throw java.lang.IllegalArgumentException("Use '\${true}' or '\${false}' for default values of ${type.name} (not $defaultValue)")
            }
            ComplexOrmTypes.Date,
            ComplexOrmTypes.LocalDate,
            ComplexOrmTypes.ByteArray,
            ComplexOrmTypes.ComplexOrmTables,
            ComplexOrmTypes.ComplexOrmTable -> {
                throw IllegalArgumentException("Default value not allowed for ${type.name} (has $defaultValue)")
            }
            ComplexOrmTypes.Float -> {
                try {
                    defaultValue.toFloat().toString()
                } catch (e: Exception) {
                    throw java.lang.IllegalArgumentException("Use something like '\${1.0}' for default values of ${type.name} (not $defaultValue)")
                }
            }
            ComplexOrmTypes.Long,
            ComplexOrmTypes.Int -> {
                try {
                    defaultValue.toLong().toString()
                } catch (e: Exception) {
                    throw java.lang.IllegalArgumentException("Use something like '\${1}' for default values of ${type.name} (not $defaultValue)")
                }
            }
        }
    }

    private fun createRelatedTableCommand(tableName: String, column: Column): String {
        val referenceTableName = rootTables.getValue(column.type.referenceTable!!).tableName
        return "\n\"\"\"CREATE TABLE IF NOT EXISTS '${tableName}_${column.columnName}'(" +
                "'${tableName}_id' INTEGER NOT NULL, " +
                "'${referenceTableName}_id' INTEGER NOT NULL, " +
                "PRIMARY KEY ('${tableName}_id', '${referenceTableName}_id'), " +
                "FOREIGN KEY ('${tableName}_id') REFERENCES '$tableName'(id), " +
                "FOREIGN KEY ('${referenceTableName}_id') REFERENCES '$referenceTableName'(id));\"\"\""
    }

    private val listType get() = List::class.asClassName().parameterizedBy(String::class.asTypeName())
    private val tableType get() = ComplexOrmTable::class.asTypeName().jvmWildcard()
    private val tableClassType get() = KClass::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(tableType))
    private val tableClassMapType get() = Map::class.asClassName().parameterizedBy(tableClassType, String::class.asTypeName())
}