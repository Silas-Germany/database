package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmAllTables
import com.github.silasgermany.complexormapi.ComplexOrmReadAlways
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import kotlin.reflect.KClass

class TableInfoExtractor(private val typeUtils: Types) {

    private val allTables = mutableListOf<Pair<Element, Boolean>>()
    private val sqlTableName = ComplexOrmTable::class.java.canonicalName
    private val allTableInfo = mutableMapOf<String, TableInfo>()


    private val allRootTableTypes: List<String> by lazy { allTables.filter { it.second }.map { "${it.first.asType()}" } }
    private val allTableTypes: List<String> by lazy { allTables.map { "${it.first.asType()}" } }
    private val allTableElements: List<String> by lazy { allTables.map { "${it.first}" } }

    fun extract(rootElements: MutableSet<out Element>): MutableMap<String, TableInfo> {
        rootElements.forEach(::extractTablesFromElement)
        allTableInfo.putAll(allTables.associate(::extractInfoFromTable))
        allTableInfo.forEach { (tableName, value) ->
            getSuperTableColumns(value).apply {
                value.columns.addAll(second)
                value.tableName = (first ?: tableName)
            }
        }
        allTableInfo.minusAssign(allTables.filter { !isTable(it.first.asType()) }.map { "${it.first}" })
        allTableInfo.values.forEach { tableInfo ->
            tableInfo.superTable
        }
        return allTableInfo
    }

    private fun getSuperTableColumns(tableInfo: TableInfo): Pair<String?, List<Column>> {
        var (tableName, columns) = tableInfo.superTable?.let {
            getSuperTableColumns(allTableInfo.getValue(it))
        } ?: Pair(null, emptyList())
        if (tableInfo.superTable in allTables.filter { it.second }.map { "${it.first}" }) {
            tableName = tableInfo.superTable
        }
        val combinedColumns = columns.filter { it.annotations.hasAnnotation(ComplexOrmReadAlways::class) }
        return tableName to combinedColumns
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
        val tableInfo = TableInfo(isColumn.mapTo(mutableListOf()) { Column(it, types.getValue(it), annotations.getValue(it)) }, isRootTable, superTable)
        return Pair("$element", tableInfo)
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


}