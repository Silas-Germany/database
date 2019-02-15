package com.github.silasgermany.complexormprocessor

class DatabaseSchemaFileCreator(private val tableInfo: MutableMap<String, TableInfo>) {

    /*
    fun createNames(): PropertySpec {
        return PropertySpec.builder("tables", tableMapType)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("mapOf(${rootTables.joinToString(",") { "\n$it::class to \"${it.sql}\"" }})")
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
                if (column.type != ComplexOrmTypes.ComplexOrmTables) null
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
    // */
}