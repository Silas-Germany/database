package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexorm.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class ProcessAllTables(tablesInterfaces: Set<Element>, processingEnv: ProcessingEnvironment) {

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