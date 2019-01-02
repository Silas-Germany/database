package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexorm.SqlDefault
import com.github.silasgermany.complexorm.SqlExtra
import com.github.silasgermany.complexorm.SqlIgnore
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import org.jetbrains.annotations.NotNull
import javax.lang.model.type.TypeMirror

interface ProcessAllTables: SqlUtils {

    fun createNames(): PropertySpec {
        return PropertySpec.builder("tableNames", listType)
            .initializer("listOf(\n${rootTables.joinToString(",\n") { "\"${it.sql}\"" }}\n)")
            .build()
    }

    fun createDropTables(): PropertySpec {
        val tableNames = rootTables.flatMap { table ->
            table.enclosedElements.mapNotNull { column ->
                val columnName = column.sql.removePrefix("get_")
                val annotations = rootAnnotations[table]?.find { "$it".startsWith(columnName) }
                if (!column.simpleName.startsWith("get")) null
                else if (annotations?.getAnnotation(SqlIgnore::class.java) != null) return@mapNotNull null
                else if (column.type != SqlUtils.SqlTypes.SqlTables) null
                else "${table.sql}_$columnName"
            }
        } + rootTables.map { it.sql }
        return PropertySpec.builder("dropTableCommands", listType)
            .initializer("listOf(\n${tableNames.joinToString(",\n") {
                "\"DROP TABLE IF EXISTS '$it'\"" }};\n)"
            )
            .build()
    }

    fun createCreateTables(): PropertySpec {
        val relatedTables = mutableListOf<String>()
        val createTableCommands = rootTables.map { table ->
            val foreignKeys = mutableListOf<String>()
            val columns = arrayOf("'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT") +
                    table.enclosedElements.mapNotNull { column ->
                        if (!column.simpleName.startsWith("get")) null
                        else {
                            val columnName = column.sql.removePrefix("get_")
                            val columnType = column.type
                            var columnExtra = ""
                            // check whether nullable
                            if (column.getAnnotation(NotNull::class.java) != null) columnExtra += " NOT NULL"
                            // check annotations
                            val annotations = rootAnnotations[table]?.find { "$it".startsWith(columnName) }
                            if (annotations?.getAnnotation(SqlIgnore::class.java) != null) return@mapNotNull null
                            val defaultValue = annotations?.getAnnotation(SqlDefault::class.java)?.value
                            columnExtra += defaultValue(columnType, defaultValue)
                            annotations?.getAnnotation(SqlExtra::class.java)?.extra?.let { columnExtra += " $it" }
                            // get type
                            val sqlType = when (columnType) {
                                SqlUtils.SqlTypes.String -> {
                                    "TEXT"
                                }
                                SqlUtils.SqlTypes.Boolean,
                                SqlUtils.SqlTypes.Date,
                                SqlUtils.SqlTypes.LocalDate,
                                SqlUtils.SqlTypes.Long,
                                SqlUtils.SqlTypes.Int -> {
                                    "INTEGER"
                                }
                                SqlUtils.SqlTypes.SqlTables -> {
                                    relatedTables.add(relatedTable(table.sql, columnName, column.asType()))
                                    return@mapNotNull null
                                }
                                SqlUtils.SqlTypes.SqlTable -> {
                                    foreignKeys.add(foreignTableReference(columnName, column.asType()))
                                    return@mapNotNull "${columnName}_id$columnExtra"
                                }
                            }
                            "'$columnName' $sqlType$columnExtra"
                        }
                    } + foreignKeys
            "\"\"\"CREATE TABLE IF NOT EXISTS '${table.sql}'(${columns.joinToString()});\"\"\""
        } + relatedTables
        return PropertySpec.builder("createTableCommands", listType)
            .initializer(CodeBlock.of("listOf(\n${createTableCommands.joinToString(",\n")}\n)"))
            .build()
    }

    private fun defaultValue(type: SqlUtils.SqlTypes, defaultValue: String?): String {
        defaultValue ?: return ""
        return " DEFAULT " + when(type) {
            SqlUtils.SqlTypes.String -> "'$defaultValue'"
            SqlUtils.SqlTypes.Boolean -> when (defaultValue) {
                "false" -> "0"
                "true" -> "1"
                else -> throw java.lang.IllegalArgumentException("Use 'false.toString()' or 'true.toString()' for boolean default values")
            }
            SqlUtils.SqlTypes.Date,
            SqlUtils.SqlTypes.LocalDate,
            SqlUtils.SqlTypes.SqlTables,
            SqlUtils.SqlTypes.SqlTable -> {
                throw IllegalArgumentException("Default value not allowed for ${type.name}: $defaultValue")
            }
            SqlUtils.SqlTypes.Long,
            SqlUtils.SqlTypes.Int -> {
                try {
                    defaultValue.toLong().toString()
                } catch (e: Exception) {
                    throw java.lang.IllegalArgumentException("Use something like '1.toString()' for default values")
                }
            }
        }
    }

    private fun foreignTableReference(columnName: String, columnType: TypeMirror): String {
        val foreignTable = columnType.toString()
            .run { substring(lastIndexOf('.') + 1) }.underScore
        return "FOREIGN KEY ('${columnName}_id') REFERENCES '$foreignTable'(id)"
    }

    private fun relatedTable(rootName: String, columnName: String, columnType: TypeMirror): String {
        val foreignTable = columnType.toString()
            .run { substring(lastIndexOf('.') + 1) }.removeSuffix(">").underScore
        return "\"\"\"CREATE TABLE IF NOT EXISTS '${rootName}_$columnName'(" +
                "'${rootName}_id' INTEGER NOT NULL, " +
                "'${foreignTable}_id' INTEGER NOT NULL, " +
                "PRIMARY KEY ('${rootName}_id', '${foreignTable}_id'), " +
                "FOREIGN KEY ('${rootName}_id') REFERENCES '$rootName'(id), " +
                "FOREIGN KEY ('${foreignTable}_id') REFERENCES '$foreignTable'(id));\"\"\""
    }
}

/*
FOREIGN KEY (creator_id) REFERENCES user(id),
CREATE TABLE IF NOT EXISTS aggregate_ministry_output(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'actual' INTEGER NOT NULL, 'comment' TEXT, 'creator_id' INTEGER NOT NULL, 'deliverable_id' INTEGER NOT NULL, 'month' TEXT NOT NULL, 'state_language_id' INTEGER NOT NULL, 'value' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (creator_id) REFERENCES user(id), FOREIGN KEY (deliverable_id) REFERENCES deliverable(id), FOREIGN KEY (state_language_id) REFERENCES state_Language(id));
CREATE TABLE IF NOT EXISTS app(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'jwt' TEXT, 'last_login' INTEGER, 'last_sync' INTEGER, 'ministry_benchmark_criteria' TEXT, 'is_online' INTEGER NOT NULL DEFAULT -1);
CREATE TABLE IF NOT EXISTS church_ministry(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'church_team_id' INTEGER NOT NULL, 'facilitator_id' INTEGER, 'ministry_id' INTEGER NOT NULL, 'status' INTEGER NOT NULL DEFAULT 0, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (church_team_id) REFERENCES church_Team(id), FOREIGN KEY (facilitator_id) REFERENCES user(id), FOREIGN KEY (ministry_id) REFERENCES ministry(id));
CREATE TABLE IF NOT EXISTS church_team(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'leader' TEXT NOT NULL, 'organisation_id' INTEGER, 'state_language_id' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (organisation_id) REFERENCES organisation(id), FOREIGN KEY (state_language_id) REFERENCES state_Language(id));

CREATE TABLE IF NOT EXISTS church_team_users(church_team_id INTEGER NOT NULL, user_id INTEGER NOT NULL, PRIMARY KEY (church_team_id, user_id),
FOREIGN KEY (church_team_id) REFERENCES user(id), FOREIGN KEY (user_id) REFERENCES church_Team(id));
CREATE TABLE IF NOT EXISTS deliverable(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'calculation_method' INTEGER NOT NULL DEFAULT 0, 'ministry_id' INTEGER NOT NULL, 'number' INTEGER NOT NULL, 'plan_form_id' INTEGER NOT NULL, 'reporter' INTEGER NOT NULL DEFAULT 0, 'result_form_id' INTEGER NOT NULL, 'short_form_id' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (ministry_id) REFERENCES ministry(id), FOREIGN KEY (plan_form_id) REFERENCES translation_Code(id), FOREIGN KEY (result_form_id) REFERENCES translation_Code(id), FOREIGN KEY (short_form_id) REFERENCES translation_Code(id));
CREATE TABLE IF NOT EXISTS edit(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'attribute_name' TEXT NOT NULL, 'created_at' INTEGER NOT NULL, 'creator_comment' TEXT, 'curated_by_id' INTEGER, 'curation_date' INTEGER, 'curator_comment' TEXT, 'model_klass_name' TEXT NOT NULL, 'new_value' TEXT NOT NULL, 'old_value' TEXT NOT NULL, 'record_errors' TEXT, 'record_id' INTEGER NOT NULL, 'relationship' INTEGER NOT NULL DEFAULT 0, 'second_curation_date' INTEGER, 'status' INTEGER NOT NULL DEFAULT 0, 'user_id' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1);
*/