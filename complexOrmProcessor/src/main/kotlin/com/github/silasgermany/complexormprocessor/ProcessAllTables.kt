package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexorm.SqlDefault
import com.github.silasgermany.complexorm.SqlExtra
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import org.jetbrains.annotations.NotNull
import javax.lang.model.element.Element

interface ProcessAllTables: SqlTypes {

    fun createNames(rootTables: List<Element>): PropertySpec {
        return PropertySpec.builder("tableNames", listType)
            .initializer("listOf(\n${rootTables.joinToString(",\n") { "\"${it.sql}\"" }}\n)")
            .build()
    }

    fun createDropTables(rootTables: List<Element>): PropertySpec {
        return PropertySpec.builder("dropTableCommands", listType)
            .initializer("listOf(\n${rootTables.joinToString(",\n") {
                "\"DROP TABLE IF EXISTS '${it.sql}'\"" }}\n)"
            )
            .build()
    }

    fun createCreateTables(rootTables: List<Element>): PropertySpec {
        val createTableCommands = rootTables.joinToString(",\n") { table ->
            val foreignKeys = mutableListOf<String>()
            val columns = arrayOf("'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT") +
                    table.enclosedElements.mapNotNull { column ->
                        if (!column.simpleName.startsWith("get")) null
                        else {
                            val columnName = column.sql.removePrefix("get_")
                            var columnExtra = ""
                            if (column.getAnnotation(NotNull::class.java) != null) columnExtra += " NOT NULL"
                            val annotations = rootAnnotations[table]?.find { "$it".startsWith(columnName) }
                            val defaultValue = annotations?.getAnnotation(SqlDefault::class.java)?.value
                            annotations?.getAnnotation(SqlExtra::class.java)?.extra?.let { columnExtra += " $it" }
                            val columnType = column.type
                            if (defaultValue != null) {
                                columnExtra += " DEFAULT " + when(columnType) {
                                    SqlTypes.SqlTypes.String -> "'$defaultValue'"
                                    SqlTypes.SqlTypes.SqlTable -> {
                                        throw IllegalArgumentException("Default value not allowed for connected tables")
                                    }
                                    else -> defaultValue
                                }
                            }
                            val sqlType = when (column.type) {
                                SqlTypes.SqlTypes.String -> {
                                    "TEXT"
                                }
                                SqlTypes.SqlTypes.Int -> {
                                    "INTEGER"
                                }
                                SqlTypes.SqlTypes.SqlTable -> {
                                    val foreignTable = column.asType().toString()
                                        .run { substring(lastIndexOf('.') + 1) }.underScore
                                    foreignKeys.add("FOREIGN KEY ('${columnName}_id') REFERENCES '$foreignTable'(id)")
                                    return@mapNotNull "${columnName}_id$columnExtra"
                                }
                            }
                            "'$columnName' $sqlType$columnExtra"
                        }
                    } + foreignKeys
            "\"\"\"CREATE TABLE IF NOT EXISTS '${table.sql}'(${columns.joinToString()})\"\"\""
        }
        return PropertySpec.builder("createTableCommands", listType)
            .initializer(CodeBlock.of("listOf(\n$createTableCommands\n)"))
            .build()
    }
}

    /*
 FOREIGN KEY (creator_id) REFERENCES user(id),
CREATE TABLE IF NOT EXISTS aggregate_ministry_output(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'actual' INTEGER NOT NULL, 'comment' TEXT, 'creator_id' INTEGER NOT NULL, 'deliverable_id' INTEGER NOT NULL, 'month' TEXT NOT NULL, 'state_language_id' INTEGER NOT NULL, 'value' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (creator_id) REFERENCES user(id), FOREIGN KEY (deliverable_id) REFERENCES deliverable(id), FOREIGN KEY (state_language_id) REFERENCES state_Language(id));
CREATE TABLE IF NOT EXISTS app(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'jwt' TEXT, 'last_login' INTEGER, 'last_sync' INTEGER, 'ministry_benchmark_criteria' TEXT, 'is_online' INTEGER NOT NULL DEFAULT -1);
CREATE TABLE IF NOT EXISTS church_ministry(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'church_team_id' INTEGER NOT NULL, 'facilitator_id' INTEGER, 'ministry_id' INTEGER NOT NULL, 'status' INTEGER NOT NULL DEFAULT 0, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (church_team_id) REFERENCES church_Team(id), FOREIGN KEY (facilitator_id) REFERENCES user(id), FOREIGN KEY (ministry_id) REFERENCES ministry(id));
CREATE TABLE IF NOT EXISTS church_team(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'leader' TEXT NOT NULL, 'organisation_id' INTEGER, 'state_language_id' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (organisation_id) REFERENCES organisation(id), FOREIGN KEY (state_language_id) REFERENCES state_Language(id));
CREATE TABLE IF NOT EXISTS church_team_users(church_team_id INTEGER NOT NULL, user_id INTEGER NOT NULL, PRIMARY KEY (church_team_id, user_id), FOREIGN KEY (church_team_id) REFERENCES user(id), FOREIGN KEY (user_id) REFERENCES church_Team(id));
CREATE TABLE IF NOT EXISTS deliverable(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'calculation_method' INTEGER NOT NULL DEFAULT 0, 'ministry_id' INTEGER NOT NULL, 'number' INTEGER NOT NULL, 'plan_form_id' INTEGER NOT NULL, 'reporter' INTEGER NOT NULL DEFAULT 0, 'result_form_id' INTEGER NOT NULL, 'short_form_id' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1, FOREIGN KEY (ministry_id) REFERENCES ministry(id), FOREIGN KEY (plan_form_id) REFERENCES translation_Code(id), FOREIGN KEY (result_form_id) REFERENCES translation_Code(id), FOREIGN KEY (short_form_id) REFERENCES translation_Code(id));
CREATE TABLE IF NOT EXISTS edit(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'attribute_name' TEXT NOT NULL, 'created_at' INTEGER NOT NULL, 'creator_comment' TEXT, 'curated_by_id' INTEGER, 'curation_date' INTEGER, 'curator_comment' TEXT, 'model_klass_name' TEXT NOT NULL, 'new_value' TEXT NOT NULL, 'old_value' TEXT NOT NULL, 'record_errors' TEXT, 'record_id' INTEGER NOT NULL, 'relationship' INTEGER NOT NULL DEFAULT 0, 'second_curation_date' INTEGER, 'status' INTEGER NOT NULL DEFAULT 0, 'user_id' INTEGER NOT NULL, 'is_online' INTEGER NOT NULL DEFAULT -1);

    private val tables = mutableMapOf(false to mutableMapOf<String, MutableList<String>>())
    private val constructorNames = mutableMapOf(false to mutableMapOf<String, MutableMap<String, String>>())
    private val normalColumns =
        mutableMapOf(false to mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>())
    private val connectedColumns =
        mutableMapOf(false to mutableMapOf<String, MutableMap<String, MutableMap<String, String?>>>())
    private val reverseConnectedColumns =
        mutableMapOf(false to mutableMapOf<String, MutableMap<String, MutableMap<String, Pair<String, String?>>>>())
    private val joinColumns =
        mutableMapOf(false to mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>())
    private val reverseJoinColumns =
        mutableMapOf(false to mutableMapOf<String, MutableMap<String, MutableMap<String, Pair<String, String>>>>())

    private val allInterfaces = mutableMapOf(false to mutableListOf<String>())
    private val interfacePackages = mutableMapOf(false to mutableMapOf<String, String>())
    private lateinit var currentInterface: String

    val stringType = String::class.asTypeName()

    val pairType = Pair::class.asClassName().parameterizedBy(stringType, stringType)
    val nullablePairType = Pair::class.asClassName().parameterizedBy(stringType, stringType.copy(true))
    val mapType = Map::class.asClassName().parameterizedBy(stringType, stringType)
    val nullableMapType = Map::class.asClassName().parameterizedBy(stringType, stringType.copy(true))

    val mapPairType = Map::class.asClassName().parameterizedBy(stringType, pairType)
    val mapNullablePairType = Map::class.asClassName().parameterizedBy(stringType, nullablePairType)
    val columnsType = Map::class.asClassName().parameterizedBy(stringType, mapType)
    val nullableColumnsType = Map::class.asClassName().parameterizedBy(stringType, nullableMapType)
    val complexColumnsType = Map::class.asClassName().parameterizedBy(stringType, mapPairType)
    val nullableComplexColumnsType = Map::class.asClassName().parameterizedBy(stringType, mapNullablePairType)
    val interfaceColumnsType = Map::class.asClassName().parameterizedBy(stringType, columnsType)
    val interfaceNullableColumnsType = Map::class.asClassName().parameterizedBy(stringType, nullableColumnsType)
    val interfaceComplexColumnsType = Map::class.asClassName().parameterizedBy(stringType, complexColumnsType)
    val interfaceNullableComplexColumnsType =
        Map::class.asClassName().parameterizedBy(stringType, nullableComplexColumnsType)

    val nullableAnyType = Any::class.asTypeName().copy(true)
    val databaseMapType = MutableMap::class.asClassName().parameterizedBy(stringType, nullableAnyType)
    val constructorType = LambdaTypeName.get(null, databaseMapType, returnType = SqlTable::class.asTypeName())
    val constructorsType = Map::class.asClassName().parameterizedBy(stringType, constructorType)
    val interfaceConstructorsType = Map::class.asClassName().parameterizedBy(stringType, constructorsType)

    val targetPackage = "com.github.silasgermany.complexorm"

    init {
        tablesInterfaces.forEach { tablesInterface ->
            currentInterface = "\"${tablesInterface.simpleName}\""
            interfacePackages.getValue(false)[currentInterface] = processingEnv.elementUtils.getPackageOf(tablesInterface).toString()
            allInterfaces.getValue(false).add(currentInterface)

            tables.getValue(false).init(currentInterface)
            normalColumns.getValue(false).init(currentInterface)
            connectedColumns.getValue(false).init(currentInterface)
            reverseConnectedColumns.getValue(false).init(currentInterface)
            joinColumns.getValue(false).init(currentInterface)
            reverseJoinColumns.getValue(false).init(currentInterface)

            getDatabaseStructure(tablesInterface)
        }
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Write: $tables");

        val generatedClassName = "GeneratedSqlTables"
        val file = FileSpec.builder(targetPackage, generatedClassName)
            .addType(
                TypeSpec.objectBuilder(generatedClassName)
                    .addProperty(
                        PropertySpec.builder("tables", List::class.asClassName().parameterizedBy(stringType))
                            .initializer("listOf(${tables.values.flatMap { it.keys }.joinToString()} )")
                            .build()
                    )
                    .build())
            .build()
        val kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
        file.writeTo(File(kaptKotlinGeneratedDir))
    }

    private fun getDatabaseStructure(tablesInterface: Element, second: Boolean = false) {
        constructorNames.getValue(second)[currentInterface] = tablesInterface.enclosedElements.associateTo(mutableMapOf()) {
            it.columnName to "${it.enclosingElement.simpleName}.${it.simpleName}"
        }
        val info = mutableListOf<String>()
        tablesInterface.enclosedElements
            .filter {
                it.getAnnotation(SqlIgnoreClass::class.java) == null &&
                        it.simpleName.toString() != "Companion"
            }
            .forEach { table ->
                tables.getValue(second)[currentInterface]!!.add(table.columnName)
                table.enclosedElements.filter {
                    it.simpleName.run {
                        !contains("delegate") && !contains("init") && !contains("set") &&
                                !contains("annotations") && !contains("getGet")
                    }
                }.forEach { column ->
                    val annotationName = "${column.columnName}\$annotations"
                    val annotationElement = table.enclosedElements
                        .find { it.simpleName.toString().underScore.toLowerCase().removePrefix("get_") == annotationName }
                    if (annotationElement?.getAnnotation(SqlIgnore::class.java) ?: column.getAnnotation(
                            SqlIgnoreFunction::class.java) == null) {
                        column.asType().toString().apply {
                            if (contains("rev79")) {
                                var joinTableName = split('.').last().underScore.toLowerCase()
                                val columnName = column.columnName
                                if (joinTableName.last() == '>') {
                                    joinTableName = joinTableName.removeSuffix(">")
                                    var hasAnnotation = false
                                    annotationElement?.getAnnotation(SqlReverseConnectedColumn::class.java)?.apply {
                                        reverseConnectedColumns.getValue(second).init(currentInterface).init(table.columnName)[columnName] =
                                                joinTableName to connectedColumn.takeUnless { it.isEmpty() }
                                        hasAnnotation = true
                                    }
                                    annotationElement?.getAnnotation(SqlReverseJoinColumn::class.java)?.apply {
                                        reverseJoinColumns.getValue(second).init(currentInterface).init(table.columnName)[columnName] = joinTableName to connectedColumn
                                        hasAnnotation = true
                                    }
                                    if (!hasAnnotation) joinColumns.getValue(second).init(currentInterface).init(table.columnName)[columnName] = joinTableName.removeSuffix(">")
                                } else {
                                    connectedColumns.getValue(second).init(currentInterface).init(table.columnName)[columnName] = joinTableName.takeUnless { it == columnName }
                                }
                            } else normalColumns.getValue(second).init(currentInterface).init(table.columnName)[column.columnName] =
                                    column.asType().toString().removePrefix("()").replace("java.lang.Boolean", "boolean")
                        }
                    }
                    info.add("${table.columnName};${column.columnName};${column.asType()};${column.asType().toString().contains("rev79")};$annotationName;${annotationElement?.simpleName};${annotationElement?.getAnnotation(
                        SqlIgnore::class.java)}")
                }
            }
        joinColumns.getValue(second).forEach { joinColumnEntry ->
            val reverseConnectedColumnInterface = reverseConnectedColumns.getValue(second)[joinColumnEntry.key]!!
            val connectedColumnInterface = connectedColumns.getValue(second)[joinColumnEntry.key]!!
            joinColumnEntry.value.toMap().forEach { interfaceEntry ->
                interfaceEntry.value.toMap().forEach { tableEntry ->
                    if (connectedColumnInterface[tableEntry.value]?.any { it.key == interfaceEntry.key && it.value == null } == true) {
                        reverseConnectedColumnInterface.init(interfaceEntry.key)[tableEntry.key] = tableEntry.value to null
                        joinColumnEntry.value[interfaceEntry.key]!!.remove(tableEntry.key)
                        if (joinColumnEntry.value[interfaceEntry.key]!!.isEmpty())
                            joinColumnEntry.value.remove(interfaceEntry.key)
                    }
                }
            }
        }

    }

    private fun PropertySpec.Builder.writeConstructors(second: Boolean = false): PropertySpec {
        mutable(false)
        allInterfaces.getValue(second).joinToString(",") {
            tables.getValue(second)[it]!!.joinToString(",") { table ->
                val constructorName = constructorNames.getValue(second)[it]!![table]
                "\n$spaces \"$table\" to { it: Map<String, Any?> ->\n    $spaces ${interfacePackages.getValue(second)[it]}.$constructorName(it as MutableMap<String, Any?>)\n$spaces }"
            }.run { "\n$it to mapOf($this)" }
        }.apply { if (isNotEmpty()) initializer("mapOf($this)") else initializer("$allInterfaces;$tables") }
        return build()
    }

    private fun PropertySpec.Builder.writeNormalColumns(second: Boolean = false): PropertySpec {
        mutable(false)
        allInterfaces.getValue(second).joinToString(",") {
            normalColumns.getValue(second)[it]!!.toList().joinToString(",") { (table, columns) ->
                val combinedColumns = columns
                    .filter { it.key != "scaled_bitmap" }
                    .toList().joinToString(transform = ::format)
                "\n$spaces \"$table\" to\n$spaces $spaces mapOf($combinedColumns)"
            }.run { "\n$it to mapOf($this)" }
        }.apply { initializer("mapOf($this)") }
        return build()
    }

    private fun PropertySpec.Builder.writeJoinColumns(second: Boolean = false): PropertySpec {
        mutable(false)
        allInterfaces.getValue(second).joinToString(",") {
            joinColumns.getValue(second)[it]!!.toList().joinToString(",") { (table, columns) ->
                val combinedColumns = columns.toList().joinToString(transform = ::format)
                "\n$spaces \"$table\" to\n$spaces $spaces mapOf($combinedColumns)"
            }.run { "\n$it to mapOf($this)" }
        }.apply { initializer("mapOf($this)") }
        return build()
    }

    private fun PropertySpec.Builder.writeReverseJoinColumns(second: Boolean = false): PropertySpec {
        mutable(false)
        allInterfaces.getValue(second).joinToString(",") {
            reverseJoinColumns.getValue(second)[it]!!.toList().joinToString(",") { (table, columns) ->
                val combinedColumns = columns.toList().joinToString(transform = ::deepFormat)
                "\n$spaces \"$table\" to\n$spaces $spaces mapOf($combinedColumns)"
            }.run { "\n$it to mapOf($this)" }
        }.apply { initializer("mapOf($this)") }
        return build()
    }

    private fun PropertySpec.Builder.writeConnectedColumns(second: Boolean = false): PropertySpec {
        mutable(false)
        allInterfaces.getValue(second).joinToString(",") {
            connectedColumns.getValue(second)[it]!!.toList()
                .joinToString(",") { (table, columns) ->
                    val combinedColumns = columns
                        .filter { column -> normalColumns.getValue(second)[it]!![table]?.containsKey("${column.key}_id") != true }
                        .toList().joinToString(transform = ::format)
                    "\n$spaces \"$table\" to\n$spaces $spaces mapOf($combinedColumns)"
                }.run { "\n$it to mapOf($this)" }
        }.apply { initializer("mapOf($this)") }
        return build()
    }

    private fun PropertySpec.Builder.writeReverseConnectedColumns(second: Boolean = false): PropertySpec {
        mutable(false)
        allInterfaces.getValue(second).joinToString(",") {
            reverseConnectedColumns.getValue(second)[it]!!.toList().joinToString(",") { (table, columns) ->
                val combinedColumns = columns.toList().joinToString(transform = ::deepFormat)
                "\n$spaces \"$table\" to\n$spaces $spaces mapOf($combinedColumns)"
            }.run { "\n$it to mapOf($this)" }
        }.apply { initializer("mapOf($this)") }
        return build()
    }

    private val String.underScore: String
        get() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2")

    private val spaces = "       "
    private val Element.columnName get() = simpleName.toString().underScore.toLowerCase().removePrefix("get_")
    private fun <T> MutableMap<String, MutableList<T>>.init(value: String) = getOrPut(value) { mutableListOf() }
    private fun <K, V> MutableMap<String, MutableMap<K, V>>.init(value: String) = getOrPut(value) { mutableMapOf() }
    private fun format(value: Pair<String, String?>) = "\"${value.first}\" to ${value.second?.let { "\"$it\"" }
        ?: "null"}"

    private fun deepFormat(value: Pair<String, Pair<String, String?>>) = "\"${value.first}\" to " +
            "(\"${value.second.first}\" to ${value.second.second?.let { "\"$it\"" } ?: "null"})"

}
*/