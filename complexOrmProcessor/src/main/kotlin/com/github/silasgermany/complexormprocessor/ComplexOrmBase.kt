package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.jvm.jvmWildcard
import javax.lang.model.element.Element
import kotlin.reflect.KClass

interface ComplexOrmBase {

    val rootTables: MutableList<Element>
    val rootAnnotations: MutableMap<String?, MutableList<Element>>
    val internAnnotations: MutableMap<Element, MutableList<Element>>
    val internTables: MutableMap<Element, MutableList<Element>>

    fun <K, V> MutableMap<K, MutableList<V>>.add(key: K, value: V) = getOrPut(key) { mutableListOf() }.add(value)

    val Element.type: ComplexOrmTypes
        get() {
            val typeName = asType().toString().removePrefix("()")
            return when (typeName) {
                "java.lang.String" -> ComplexOrmTypes.String
                "java.lang.Integer", "int" -> ComplexOrmTypes.Int
                "java.lang.Boolean", "boolean" -> ComplexOrmTypes.Boolean
                "java.util.Date" -> ComplexOrmTypes.Date
                "org.threeten.bp.LocalDate" -> ComplexOrmTypes.LocalDate
                "byte[]" -> ComplexOrmTypes.LocalDate
                "long" -> ComplexOrmTypes.Long
                "java.lang.Long" -> ComplexOrmTypes.Long
                "java.lang.Float", "float" -> ComplexOrmTypes.Float
                else -> {
                    try {
                        if (typeName.startsWith("java.util.List")) return ComplexOrmTypes.ComplexOrmTables
                        if (rootTables.any { it.toString() == typeName }) return ComplexOrmTypes.ComplexOrmTable
                        if (internTables.any { file -> file.value.any { it.toString() == typeName } }) {
                            return ComplexOrmTypes.ComplexOrmTable
                        }
                    } catch (e: Exception) {
                        throw IllegalArgumentException("Problem (${e.message}) with $typeName;")
                    }
                    throw IllegalArgumentException("Couldn't find type ${asType()}: $this")
                }
            }
        }

    val Element.sql: String
        get() = simpleName.sql

    val CharSequence.sql: String
        get() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()

    val CharSequence.reverseUnderScore: String
        get() = replace("_[a-zA-Z]".toRegex()) { match -> match.value[1].toUpperCase().toString() }

    val stringType get() = String::class.asTypeName()

    val pairType get() = Pair::class.asClassName().parameterizedBy(stringType, stringType)
    val nullablePairType get() = Pair::class.asClassName().parameterizedBy(stringType, stringType.copy(true))
    val mapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType)
    val mapType2 get() = Map::class.asClassName().parameterizedBy(stringType, ComplexOrmTypes::class.asTypeName())
    val nullableMapType get() = Map::class.asClassName().parameterizedBy(stringType, stringType.copy(true))

    val listType get() = List::class.asClassName().parameterizedBy(String::class.asTypeName())
    val tableType get() = ComplexOrmTable::class.asTypeName().jvmWildcard()//TypeVariableName("ComplexOrmTable", KModifier.OUT)
    val tableClassType get() = KClass::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(tableType))
    val tableMapType get() = Map::class.asClassName().parameterizedBy(tableClassType, String::class.asTypeName())

    val mapPairType get() = Map::class.asClassName().parameterizedBy(stringType, pairType)
    val mapNullablePairType get() = Map::class.asClassName().parameterizedBy(stringType, nullablePairType)
    val columnsType get() = Map::class.asClassName().parameterizedBy(stringType, mapType)
    val columnsType2 get() = Map::class.asClassName().parameterizedBy(stringType, mapType2)
    val nullableColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, nullableMapType)
    val complexColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, mapPairType)
    val nullableComplexColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, mapNullablePairType)
    val interfaceColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, columnsType)
    val interfaceColumnsType2 get() = Map::class.asClassName().parameterizedBy(stringType, columnsType2)
    val interfaceNullableColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, nullableColumnsType)
    val interfaceComplexColumnsType get() = Map::class.asClassName().parameterizedBy(stringType, complexColumnsType)
    val interfaceNullableComplexColumnsType
        get() = Map::class.asClassName().parameterizedBy(
            stringType,
            nullableComplexColumnsType
        )

    val nullableAnyType get() = Any::class.asTypeName().copy(true)
    val databaseMapType get() = MutableMap::class.asClassName().parameterizedBy(stringType, nullableAnyType)
    val constructorType get() = LambdaTypeName.get(null, databaseMapType, returnType = ComplexOrmTable::class.asTypeName())
    val constructorsType get() = Map::class.asClassName().parameterizedBy(stringType, constructorType)
    val interfaceConstructorsType get() = Map::class.asClassName().parameterizedBy(stringType, constructorsType)
}