package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexorm.SqlTable
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.util.Types

interface SqlUtils {

    val messager: Messager
    val typeUtils: Types
    val rootTables: MutableList<Element>
    val rootAnnotations: MutableMap<Element, MutableList<Element>>

    fun <K, V> MutableMap<K, MutableList<V>>.add(key: K, value: V) = getOrPut(key) { mutableListOf() }.add(value)

    enum class SqlTypes {
        String, Int, Boolean, Long, Date, LocalDate, SqlTable, SqlTables
    }

    val Element.type: SqlTypes
        get() {
            val typeName = asType().toString().removePrefix("()")
            return when (typeName) {
                "java.lang.String" -> SqlTypes.String
                "java.lang.Integer" -> SqlTypes.Int
                "boolean" -> SqlTypes.Boolean
                "java.util.Date" -> SqlTypes.Date
                "org.threeten.bp.LocalDate" -> SqlTypes.LocalDate
                "long" -> SqlTypes.Long
                else -> {
                    try {
                        if (typeName.startsWith("java.util.List")) return SqlTypes.SqlTables
                        if (rootTables.any { it.toString() == typeName }) return SqlTypes.SqlTable
                    } catch (e: Exception) {
                        throw IllegalArgumentException("Problem (${e.message}) with $typeName;")
                    }
                    throw IllegalArgumentException("Couldn't find type ${asType()}")
                }
            }
        }

    val Element.sql: String
        get() = simpleName.underScore

    val CharSequence.underScore: String
        get() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()

    val CharSequence.reverseUnderScore: String
        get() = replace("_[a-zA-Z]".toRegex()) { match -> match.value[1].toUpperCase().toString() }


    val stringType get() = String::class.asTypeName()

    val pairType get() = Pair::class.asClassName().parameterizedBy(stringType, stringType)
    val nullablePairType get() = Pair::class.asClassName().parameterizedBy(stringType, stringType.copy(true))
    val mapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType)
    val nullableMapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType.copy(true))

    val listType get() = List::class.asClassName().parameterizedBy(String::class.asTypeName())

    val mapPairType get() = Map::class.asClassName().parameterizedBy(stringType, pairType)
    val mapNullablePairType get() = Map::class.asClassName().parameterizedBy(stringType, nullablePairType)
    val columnsType get() = Map::class.asClassName().parameterizedBy(stringType, mapType)
    val nullableColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, nullableMapType)
    val complexColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, mapPairType)
    val nullableComplexColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, mapNullablePairType)
    val interfaceColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, columnsType)
    val interfaceNullableColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, nullableColumnsType)
    val interfaceComplexColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, complexColumnsType)
    val interfaceNullableComplexColumnsType
        get() = Map::class.asClassName().parameterizedBy(
            stringType,
            nullableComplexColumnsType
        )

    val nullableAnyType get() = Any::class.asTypeName().copy(true)
    val databaseMapType get() = MutableMap::class.asClassName().parameterizedBy(stringType, nullableAnyType)
    val constructorType get() = LambdaTypeName.get(null, databaseMapType, returnType = SqlTable::class.asTypeName())
    val constructorsType get() = Map::class.asClassName().parameterizedBy(stringType, constructorType)
    val interfaceConstructorsType get() = Map::class.asClassName().parameterizedBy(stringType, constructorsType)
}