package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.*
import com.github.silasgermany.complexormprocessor.models.Column
import com.github.silasgermany.complexormprocessor.models.TableInfo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmWildcard
import kotlin.reflect.KClass

class FileCreatorDatabaseSchema(tableInfo: MutableMap<String, TableInfo>) {

    private val rootTables = tableInfo.filter { it.value.isRoot }
    private val rootTablesList = rootTables.toList()

    fun createNames(): PropertySpec {
        return PropertySpec.builder("tables", tableClassMapType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf(${rootTablesList.joinToString(",") { "\n${it.first}::class to \"${it.second.tableName}\"" }})")
            .build()
    }

    fun createDropTables(): PropertySpec {
        return PropertySpec.builder("dropTableCommands", stringMapType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf(${rootTablesList.joinToString(",") {
                val tableName = it.second.tableName
                "\n\"$tableName\" to \"DROP TABLE IF EXISTS '$tableName';\"" }})"
            )
            .build()
    }

    fun createCreateTables(): PropertySpec {
        val relatedTables = mutableListOf<String>()
        val createTableCommands = rootTablesList.map { (_, tableInfo) ->
            val writtenColumns = mutableSetOf<String>()
            val foreignKeys = mutableListOf<String>()
            val columns = arrayOf("'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT") +
                    tableInfo.columns.mapNotNull { column ->
                        if (!writtenColumns.add(column.idName)) return@mapNotNull null
                        var columnExtra = ""
                        // check whether nullable
                        if (!column.columnType.nullable) columnExtra += " NOT NULL"
                        // check annotations
                        column.getAnnotationValue(ComplexOrmDefault::class)?.let {
                            columnExtra += defaultValue(column.columnType.type, "$it")
                        }
                        column.getAnnotationValue(ComplexOrmProperty::class)?.let {
                            columnExtra += " $it".replace(column.name, column.columnName)
                        }
                        column.getAnnotationValue(ComplexOrmUnique::class)?.let {
                            columnExtra += " UNIQUE"
                        }
                        // get type
                        val complexOrmType = when (column.columnType.type) {
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
                                val referenceTableName = rootTables.getValue(column.columnType.referenceTable!!).tableName
                                foreignKeys.add("FOREIGN KEY ('${column.idName}') REFERENCES '$referenceTableName'(id)")
                                return@mapNotNull "'${column.idName}' INTEGER$columnExtra"
                            }
                        }
                        "'${column.columnName}' $complexOrmType$columnExtra"
                    } + foreignKeys
            "\n\"${tableInfo.tableName}\" to \"\"\"CREATE TABLE IF NOT EXISTS '${tableInfo.tableName}'(${columns.joinToString()});\"\"\""
        } + relatedTables
        return PropertySpec.builder("createTableCommands", stringMapType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer(CodeBlock.of("mapOf(${createTableCommands.joinToString(",")})"))
            .build()
    }

    private fun defaultValue(type: ComplexOrmTypes, defaultValue: String?): String {
        defaultValue ?: return ""
        return " DEFAULT " + when(type) {
            ComplexOrmTypes.String -> "'$defaultValue'"
            ComplexOrmTypes.Boolean -> when (defaultValue) {
                "false" -> "0"
                "true" -> "1"
                else -> throw java.lang.IllegalArgumentException("Use \"\${true}\" or \"\${false}\" for default values of ${type.name} (not $defaultValue)")
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
                    throw java.lang.IllegalArgumentException("Use something like \"\${1.0}\" for default values of ${type.name} (not $defaultValue)")
                }
            }
            ComplexOrmTypes.Long,
            ComplexOrmTypes.Int -> {
                try {
                    defaultValue.toLong().toString()
                } catch (e: Exception) {
                    throw java.lang.IllegalArgumentException("Use something like \"\${1}\" for default values of ${type.name} (not $defaultValue)")
                }
            }
        }
    }

    private fun createRelatedTableCommand(tableName: String, column: Column): String {
        val referenceTableName = rootTables.getValue(column.columnType.referenceTable!!).tableName
        return "\n\"${tableName}_${column.columnName}\" to \"\"\"CREATE TABLE IF NOT EXISTS '${tableName}_${column.columnName}'(" +
                "'${tableName}_id' INTEGER NOT NULL, " +
                "'${referenceTableName}_id' INTEGER NOT NULL, " +
                "PRIMARY KEY ('${tableName}_id', '${referenceTableName}_id'), " +
                "FOREIGN KEY ('${tableName}_id') REFERENCES '$tableName'(id), " +
                "FOREIGN KEY ('${referenceTableName}_id') REFERENCES '$referenceTableName'(id));\"\"\""
    }

    private val stringType get() = String::class.asTypeName()
    private val stringMapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType)

    private val tableType get() = ComplexOrmTable::class.asTypeName().jvmWildcard()
    private val tableClassType get() = KClass::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(tableType))
    private val tableClassMapType get() = Map::class.asClassName().parameterizedBy(tableClassType, String::class.asTypeName())
}