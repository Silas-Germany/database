package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import org.jetbrains.annotations.NotNull
import javax.lang.model.type.TypeMirror

interface ProcessAllTables: ComplexOrmUtils {

    fun createNames(): PropertySpec {
        return PropertySpec.builder("tableNames", listType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("listOf(${rootTables.joinToString(",") { "\n\"${it.sql}\"" }})")
            .build()
    }

    fun createTables(): PropertySpec {
        return PropertySpec.builder("tables", tableListType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("listOf(${rootTables.joinToString(",") { "\n${it}::class" }})")
            .build()
    }

    fun createDropTables(): PropertySpec {
        //throw IllegalAccessException("${rootAnnotations["progress_marker"]?.map { it?.getAnnotation(ComplexOrmIgnore::class.java).toString() + "+$it" }}x")
        val tableNames = rootTables.flatMap { table ->
            table.enclosedElements.mapNotNull { column ->
                if (!column.simpleName.startsWith("get")) return@mapNotNull null
                if (column.asType().toString().startsWith("()kotlin.jvm.functions.Function")) return@mapNotNull null
                val columnName = column.sql.removePrefix("get_")
                val annotations = rootAnnotations[table.sql]?.find { "$it".sql.startsWith(columnName) }
                if (annotations?.getAnnotation(ComplexOrmIgnore::class.java) != null) null
                else if (column.type != ComplexOrmTypes.ComplexOrmTables) null
                else "${table.sql}_$columnName"
            }
        } + rootTables.map { it.sql }
        return PropertySpec.builder("dropTableCommands", listType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("listOf(${tableNames.joinToString(",") {
                "\n\"DROP TABLE IF EXISTS '$it';\"" }})"
            )
            .build()
    }

    fun createCreateTables(): PropertySpec {
        val relatedTables = mutableListOf<String>()
        val createTableCommands = rootTables.map { table ->
            val foreignKeys = mutableListOf<String>()
            val columns = arrayOf("'_id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT") +
                    table.enclosedElements.mapNotNull { column ->
                        if (!column.simpleName.startsWith("get")
                            || column.asType().toString().startsWith("()kotlin.jvm.functions.Function")) {
                            return@mapNotNull null
                        }
                        else {
                            val columnName = column.sql.removePrefix("get_")
                            var columnExtra = ""
                            // check whether nullable
                            if (column.getAnnotation(NotNull::class.java) != null) columnExtra += " NOT NULL"
                            // check annotations
                            val annotations = rootAnnotations[table.sql]?.find { "$it".sql.startsWith(columnName) }
                            if (annotations?.getAnnotation(ComplexOrmIgnore::class.java) != null) return@mapNotNull null
                            val columnType = column.type
                            annotations?.getAnnotation(ComplexOrmDefault::class.java)?.value?.let {
                                columnExtra += defaultValue(columnType, it)
                            }
                            annotations?.getAnnotation(ComplexOrmProperty::class.java)?.extra?.let { columnExtra += " $it" }
                            annotations?.getAnnotation(ComplexOrmUnique::class.java)?.let { columnExtra += " UNIQUE" }
                            // get type
                            val complexOrmType = when (columnType) {
                                ComplexOrmTypes.String -> {
                                    "TEXT"
                                }
                                ComplexOrmTypes.Boolean,
                                ComplexOrmTypes.Date,
                                ComplexOrmTypes.LocalDate -> {
                                    "NUMERIC"
                                }
                                ComplexOrmTypes.Long,
                                ComplexOrmTypes.Int -> {
                                    "INTEGER"
                                }
                                ComplexOrmTypes.Float -> {
                                    "REAL"
                                }
                                ComplexOrmTypes.ByteArray -> "BLOB"
                                ComplexOrmTypes.ComplexOrmTables -> {
                                    relatedTables.add(relatedTable(table.sql, columnName, column.asType()))
                                    return@mapNotNull null
                                }
                                ComplexOrmTypes.ComplexOrmTable -> {
                                    foreignKeys.add(foreignTableReference(columnName, column.asType()))
                                    return@mapNotNull "${columnName}_id$columnExtra"
                                }
                            }
                            "'$columnName' $complexOrmType$columnExtra"
                        }
                    } + foreignKeys
            "\n\"\"\"CREATE TABLE IF NOT EXISTS '${table.sql}'(${columns.joinToString()});\"\"\""
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
                else -> throw java.lang.IllegalArgumentException("Use 'false.toString()' or 'true.toString()' for boolean default values (not $defaultValue)")
            }
            ComplexOrmTypes.Date,
            ComplexOrmTypes.LocalDate,
            ComplexOrmTypes.ComplexOrmTables,
            ComplexOrmTypes.ComplexOrmTable -> {
                throw IllegalArgumentException("Default value not allowed for ${type.name}: $defaultValue")
            }
            ComplexOrmTypes.ByteArray -> "$defaultValue"
            ComplexOrmTypes.Float -> {
                try {
                    defaultValue.toFloat().toString()
                } catch (e: Exception) {
                    throw java.lang.IllegalArgumentException("Use something like '1.toString()' for default values (not $defaultValue)")
                }
            }
            ComplexOrmTypes.Long,
            ComplexOrmTypes.Int -> {
                try {
                    defaultValue.toLong().toString()
                } catch (e: Exception) {
                    throw java.lang.IllegalArgumentException("Use something like '1.toString()' for default values (not $defaultValue)")
                }
            }
        }
    }

    private fun foreignTableReference(columnName: String, columnType: TypeMirror): String {
        val foreignTable = columnType.toString()
            .run { substring(lastIndexOf('.') + 1) }.sql
        return "FOREIGN KEY ('${columnName}_id') REFERENCES '$foreignTable'(_id)"
    }

    private fun relatedTable(rootName: String, columnName: String, columnType: TypeMirror): String {
        val foreignTable = columnType.toString()
            .run { substring(lastIndexOf('.') + 1) }.removeSuffix(">").sql
        return "\"\"\"CREATE TABLE IF NOT EXISTS '${rootName}_$columnName'(" +
                "'${rootName}_id' INTEGER NOT NULL, " +
                "'${foreignTable}_id' INTEGER NOT NULL, " +
                "PRIMARY KEY ('${rootName}_id', '${foreignTable}_id'), " +
                "FOREIGN KEY ('${rootName}_id') REFERENCES '$rootName'(_id), " +
                "FOREIGN KEY ('${foreignTable}_id') REFERENCES '$foreignTable'(_id));\"\"\""
    }
}

/*
FOREIGN KEY (creator_id) REFERENCES user(_id),
CREATE TABLE IF NOT EXISTS aggregate_ministry_output(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'actual' INTEGER NOT NULL, 'comment' TEXT, 'creator_id' INTEGER NOT NULL, 'deliverable_id' INTEGER NOT NULL, 'month' TEXT NOT NULL, 'state_language_id' INTEGER NOT NULL, 'value' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (creator_id) REFERENCES user(_id), FOREIGN KEY (deliverable_id) REFERENCES deliverable(_id), FOREIGN KEY (state_language_id) REFERENCES state_Language(_id));
CREATE TABLE IF NOT EXISTS app(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'jwt' TEXT, 'last_login' INTEGER, 'last_sync' INTEGER, 'ministry_benchmark_criteria' TEXT, 'is_online' INTEGER NOT NULL DEFAULT -1);
CREATE TABLE IF NOT EXISTS church_ministry(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'church_team_id' INTEGER NOT NULL, 'facilitator_id' INTEGER, 'ministry_id' INTEGER NOT NULL, 'status' INTEGER NOT NULL DEFAULT 0, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (church_team_id) REFERENCES church_Team(_id), FOREIGN KEY (facilitator_id) REFERENCES user(_id), FOREIGN KEY (ministry_id) REFERENCES ministry(_id));
CREATE TABLE IF NOT EXISTS church_team(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'leader' TEXT NOT NULL, 'organisation_id' INTEGER, 'state_language_id' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (organisation_id) REFERENCES organisation(_id), FOREIGN KEY (state_language_id) REFERENCES state_Language(_id));

CREATE TABLE IF NOT EXISTS church_team_users(church_team_id INTEGER NOT NULL, user_id INTEGER NOT NULL, PRIMARY KEY (church_team_id, user_id),
FOREIGN KEY (church_team_id) REFERENCES user(_id), FOREIGN KEY (user_id) REFERENCES church_Team(_id));
CREATE TABLE IF NOT EXISTS deliverable(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'calculation_method' INTEGER NOT NULL DEFAULT 0, 'ministry_id' INTEGER NOT NULL, 'number' INTEGER NOT NULL, 'plan_form_id' INTEGER NOT NULL, 'reporter' INTEGER NOT NULL DEFAULT 0, 'result_form_id' INTEGER NOT NULL, 'short_form_id' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (ministry_id) REFERENCES ministry(_id), FOREIGN KEY (plan_form_id) REFERENCES translation_Code(_id), FOREIGN KEY (result_form_id) REFERENCES translation_Code(_id), FOREIGN KEY (short_form_id) REFERENCES translation_Code(_id));
CREATE TABLE IF NOT EXISTS edit(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'attribute_name' TEXT NOT NULL, 'created_at' INTEGER NOT NULL, 'creator_comment' TEXT, 'curated_by_id' INTEGER, 'curation_date' INTEGER, 'curator_comment' TEXT, 'model_klass_name' TEXT NOT NULL, 'new_value' TEXT NOT NULL, 'old_value' TEXT NOT NULL, 'record_errors' TEXT, 'record_id' INTEGER NOT NULL, 'relationship' INTEGER NOT NULL DEFAULT 0, 'second_curation_date' INTEGER, 'status' INTEGER NOT NULL DEFAULT 0, 'user_id' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1);
*/