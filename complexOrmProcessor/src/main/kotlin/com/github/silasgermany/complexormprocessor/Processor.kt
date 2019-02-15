package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.*
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.KClass

class Processor(private val processingEnvironment: ProcessingEnvironment): ComplexOrmBase, ProcessAllTables, ProcessNormalTables {


    private val targetPackage = "com.github.silasgermany.complexorm"
    private val kaptKotlinGeneratedDir = processingEnvironment.options["kapt.kotlin.generated"]!!
    private val messager = processingEnvironment.messager
    private val typeUtils = processingEnvironment.typeUtils
    private val sqlTableName = ComplexOrmTable::class.java.canonicalName

    private val allTables = mutableListOf<Pair<Element, Boolean>>()

    fun process(roundEnv: RoundEnvironment) {
        roundEnv.rootElements.forEach(::extractTablesFromElement)
        val rootTableInfo = allTables.associateTo(mutableMapOf(), ::extractInfoFromTable)
        rootTableInfo.forEach { (_, value) ->
            value.columns.add(Column("id", ColumnType(ComplexOrmTypes.Long, true, null), emptyList()))
            value.superTable?.let { superTableName ->
                rootTableInfo[superTableName]?.columns
                    ?.filter { it.annotations.hasAnnotation(ComplexOrmReadAlways::class)}
                    ?.let { value.columns.addAll(it) }
            }
        }
        rootTableInfo.minusAssign(allTables.filter { !isTable(it.first.asType()) }.map { "${it.first}" })
        messager.printMessage(Diagnostic.Kind.ERROR, "$rootTableInfo")
    }

    private fun List<AnnotationMirror>.hasAnnotation(annotation: KClass<out Annotation>)
            = any { "$it".removePrefix("@") == annotation.java.canonicalName }

    private fun extractTablesFromElement(element: Element) {
        element.enclosedElements.forEach(::extractTablesFromElement)
        if (element.kind.isClass && inheritsFromComplexOrmTable(element.asType())) {
            allTables.add(element to isRootTable(element))
        }
    }

    private fun isRootTable(element: Element)
            = element.enclosingElement.getAnnotation(ComplexOrmAllTables::class.java) != null

    private fun inheritsFromComplexOrmTable(type: TypeMirror): Boolean {
        if (type.kind == TypeKind.EXECUTABLE ) return false
        if ("$type" == sqlTableName) return true
        return typeUtils.directSupertypes(type).any(::inheritsFromComplexOrmTable)
    }

    private val allRootTableTypes: List<String> by lazy { allTables.filter { it.second }.map { "${it.first.asType()}" } }
    private val allTableTypes: List<String> by lazy { allTables.map { "${it.first.asType()}" } }
    private val allTableElements: List<String> by lazy { allTables.map { "${it.first}" } }
    private fun isTable(type: TypeMirror): Boolean {
        if ("$type" in allRootTableTypes) return true
        return typeUtils.directSupertypes(type).any(::isTable)
    }

    private fun extractInfoFromTable(tableData: Pair<Element, Boolean>): Pair<String, TableInfo> {
        val (element, isRootTable) = tableData
        val isColumn = mutableListOf<String>()
        val types = mutableMapOf<String, ColumnType>()
            .withDefault { columnName ->
                throw IllegalArgumentException("Type of column $columnName in table ${element.simpleName} is not valid: " +
                    "It has to be one of the following types: ${ComplexOrmTypes.values().map { it.name }}") }
        val annotations = mutableMapOf<String, List<AnnotationMirror>>()
            .withDefault { emptyList() }
        element.enclosedElements.forEach { value ->
            if ("${value.simpleName}".endsWith("\$delegate") && "${value.asType()}" == "java.util.Map") {
                isColumn.add("${value.simpleName}".removeSuffix("\$delegate"))
            } else if ("${value.simpleName}".endsWith("\$annotations")) {
                annotations["${value.simpleName}".removeSuffix("\$annotations")] = value.annotationMirrors
            } else if ("${value.simpleName}".startsWith("get")) {
                val valueName = "${value.simpleName}".removePrefix("get")
                    .run { first().toLowerCase() + substring(1) }
                getComplexOrmTypes(value.asType())?.let {
                    val table = when(it) {
                        ComplexOrmTypes.ComplexOrmTable -> "${value.asType()}".removePrefix("()")
                        ComplexOrmTypes.ComplexOrmTables -> "${value.asType()}".removeSurrounding("()java.util.List<", ">")
                        else -> null
                    }
                    types[valueName] = ColumnType(it, isNullable(value), table)
                }
            }
        }
        val superTable = if (isRootTable) null
        else typeUtils.directSupertypes(element.asType()).find { "$it" in allTableElements }?.toString()
        return Pair("$element", TableInfo(isColumn.mapTo(mutableListOf()) { Column(it, types.getValue(it), annotations.getValue(it)) }, superTable))
    }

    private fun isNullable(element: Element): Boolean {
        val typeName = element.asType().toString().removePrefix("()")
        return when (typeName) {
            "boolean", "int", "long", "float" -> false
            else -> {
                return when {
                    element.getAnnotation(Nullable::class.java) != null -> {
                        if (typeName.startsWith("java.util.List"))
                            throw IllegalArgumentException("Connected lists should be not nullable - an empty list will be returned, if no entry was found: $element")
                        true
                    }
                    element.getAnnotation(NotNull::class.java) != null -> false
                    else -> throw IllegalStateException("Should have annotation, whether value nullable or not")
                }
            }
        }
    }

    private fun getComplexOrmTypes(type: TypeMirror): ComplexOrmTypes? {
        val typeName = type.toString().removePrefix("()")
        return when (typeName) {
            "boolean", "java.lang.Boolean" -> ComplexOrmTypes.Boolean
            "int", "java.lang.Integer" -> ComplexOrmTypes.Int
            "long", "java.lang.Long" -> ComplexOrmTypes.Long
            "float", "java.lang.Float" -> ComplexOrmTypes.Float
            "java.lang.String" -> ComplexOrmTypes.String
            "java.util.Date" -> ComplexOrmTypes.Date
            "org.threeten.bp.LocalDate" -> ComplexOrmTypes.LocalDate
            "byte[]" -> ComplexOrmTypes.LocalDate
            else -> {
                return when {
                    typeName.startsWith("java.util.List") -> ComplexOrmTypes.ComplexOrmTables
                    typeName in allTableTypes -> ComplexOrmTypes.ComplexOrmTable
                    else -> null
                }
            }
        }
    }



    override val rootTables = mutableListOf<Element>()
    override val rootAnnotations = mutableMapOf<String?, MutableList<Element>>()
    override val internAnnotations = mutableMapOf<Element, MutableList<Element>>()
    override val internTables = mutableMapOf<Element, MutableList<Element>>()

    fun process2(roundEnv: RoundEnvironment): Boolean {
        try {
            roundEnv.rootElements.forEach { fileElement ->
                fileElement.enclosedElements.forEach enclosedElement@{ enclosedElement ->
                    try {
                        //allTables2.getOrPut(fileElement) { mutableListOf() }.add(Table2(enclosedElement))
                        if (enclosedElement.asType().toString().startsWith("kotlin.jvm.functions.Function0") ||
                            enclosedElement.asType().kind == TypeKind.EXECUTABLE ) return@enclosedElement
                        typeUtils.directSupertypes(enclosedElement.asType()).forEach { superType ->
                            if (fileElement.getAnnotation(ComplexOrmAllTables::class.java) != null &&
                                sqlTableName == superType.toString()) rootTables.add(enclosedElement)
                            typeUtils.directSupertypes(superType).forEach {
                                if (sqlTableName == it.toString()) internTables.add(fileElement, enclosedElement)
                            }
                        }
                    } catch (e: Exception) {
                        messager.printMessage(
                            Diagnostic.Kind.NOTE,
                            "Problem (${e.message}) with: $fileElement; $enclosedElement"
                        )
                    }
                }
            }
            rootTables.forEach { rootTable ->
                rootTable.enclosedElements.forEach {
                    if ("$it".endsWith("\$annotations()")) rootAnnotations.add(rootTable.sql, it)
                }
            }
            internTables.forEach {
                it.value.forEach { table ->
                    table.enclosedElements.forEach { column ->
                        if ("$column".endsWith("\$annotations()")) internAnnotations.add(table, column)
                    }
                }
            }
            var fileName = "ComplexOrmSchema"
            var file = FileSpec.builder(targetPackage, fileName)
                .addType(
                    TypeSpec.objectBuilder(fileName)
                        .addSuperinterface(ComplexOrmSchemaInterface::class)
                        .addProperty(createNames())
                        .addProperty(createDropTables())
                        .addProperty(createCreateTables())
                        .build()
                ).build()
            file.writeTo(File(kaptKotlinGeneratedDir))
            fileName = "ComplexOrmTables"
            file = FileSpec.builder(targetPackage, fileName)
                .addType(
                    TypeSpec.objectBuilder(fileName)
                        .addSuperinterface(ComplexOrmTablesInterface::class)
                        .addProperty(createConstructors())
                        .addProperty(createNormalColumnsInfo())
                        .addProperty(createJoinColumnsInfo())
                        .addProperty(createConnectedColumnsInfo())
                        .addProperty(createReverseConnectedColumnsInfo())
                        .build()
                ).build()
            file.writeTo(File(kaptKotlinGeneratedDir))
            //messager.printMessage(Diagnostic.Kind.WARNING, "${allTables2.values.flatten().filter { it.isTable }}")
            return true
        } catch (e: Exception) {
            messager.printMessage(Diagnostic.Kind.ERROR, "${e.message} at: ${e.stackTrace.joinToString()}")
            return false
        }
    }
}