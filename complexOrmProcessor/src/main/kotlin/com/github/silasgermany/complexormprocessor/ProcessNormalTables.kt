package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexorm.SqlIgnore
import com.squareup.kotlinpoet.PropertySpec
import javax.lang.model.element.Element

interface ProcessNormalTables: SqlUtils {

    val internTables: MutableMap<Element, MutableList<Element>>

    fun createConstructors(): PropertySpec {
        val constructors = internTables.toList().joinToString(",") { (file, tables) ->
            "\n\"${file.simpleName}\" to mapOf(" + tables.joinToString(",", postfix = ")"){ table ->
                "\n\t\"${table.sql}\" to { it: Map<String, Any?> -> $table(it as MutableMap<String, Any?>)}"
            }
        }
        return PropertySpec.builder("constructors", interfaceConstructorsType)
            .initializer("mapOf($constructors)")
            .build()
    }

    fun createNormalColumnsInfo(): PropertySpec {
        val normalColumnInfo = internTables.toList().joinToString(",") { (file, tables) ->
            "\n\"${file.simpleName}\" to mapOf(" + tables.joinToString(",", postfix = ")"){ table ->
                "\n\t\"${table.sql}\" to mapOf(" + table.enclosedElements.mapNotNull { column ->
                    val columnName = column.sql.removePrefix("get_")
                    val annotations = rootAnnotations[table]?.find { "$it".startsWith(columnName) }
                    if (!column.simpleName.startsWith("get")) null
                    else if (annotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (column.type == SqlUtils.SqlTypes.SqlTables) null
                    else "\n\t\t\"$columnName\" to \"${column.asType().toString().removePrefix("()")}\""
                }.joinToString(",", postfix = ")")
            }
        }
        return PropertySpec.builder("normalColumns", interfaceColumnsType)
            .initializer("mapOf($normalColumnInfo)")
            .build()
    }

    fun createJoinColumnsInfo(): PropertySpec {
        val joinColumnInfo = internTables.toList().mapNotNull { (file, tables) ->
            val fileInfo = tables.mapNotNull { table ->
                val tableInfo = table.enclosedElements.mapNotNull { column ->
                    val columnName = column.sql.removePrefix("get_")
                    val annotations = rootAnnotations[table]?.find { "$it".startsWith(columnName) }
                    if (!column.simpleName.startsWith("get")) null
                    else if (annotations?.getAnnotation(SqlIgnore::class.java) != null) null
                    else if (column.type != SqlUtils.SqlTypes.SqlTables) null
                    else {
                        val foreignTableName = column.asType().toString()
                            .run { substring(lastIndexOf('.') + 1) }
                            .removeSuffix(">").underScore
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
            .initializer("mapOf($joinColumnInfo)")
            .build()
    }
}
/*
abstract class ProcessNormalTables(tablesInterfaces: Set<Element>, processingEnv: ProcessingEnvironment): SqlUtils
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

    val targetPackage = "com.github.silasgermany.complexorm"

    init {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Create database table");

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
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Write: $constructorNames");
        val fileName = "GeneratedSqlTables"
        val file = FileSpec.builder(targetPackage, fileName)
            .addType(
                TypeSpec.objectBuilder(fileName)
                    .addProperty(
                        PropertySpec.builder("constructors", interfaceConstructorsType)
                            .writeConstructors())
                    .addProperty(
                        PropertySpec.builder("normalColumns", interfaceColumnsType)
                            .writeNormalColumns())
                    .addProperty(
                        PropertySpec.builder("joinColumns", interfaceColumnsType)
                            .writeJoinColumns())
                    .addProperty(
                        PropertySpec.builder("reverseJoinColumns", interfaceComplexColumnsType)
                            .writeReverseJoinColumns())
                    .addProperty(
                        PropertySpec.builder("connectedColumns", interfaceNullableColumnsType)
                            .writeConnectedColumns())
                    .addProperty(
                        PropertySpec.builder("reverseConnectedColumns", interfaceNullableComplexColumnsType)
                            .writeReverseConnectedColumns())
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