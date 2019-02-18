package com.github.silasgermany.complexorm.oldVersion

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.jvmErasure

abstract class ComplexOrmUtils {

    inline val <reified T : ComplexOrmTable> KProperty1<T, *>.fullName get() = "${T::class.tableName}.$columnName"

    val KProperty1<out ComplexOrmTable, *>.columnName get() =
        if (complexOrmSchema.tables.values.contains(returnType.jvmErasure.simpleName!!.sql)) "${name}_id"
        else name

    val KClass<out ComplexOrmTable>.tableName get() = simpleName!!.sql.takeIf { complexOrmSchema.tables.values.contains(it) }
        ?: superclasses.find { complexOrmSchema.tables.values.contains(it.simpleName?.sql) }?.simpleName?.sql!!


    fun <K, V> MutableMap<K, MutableList<V>>.add(key: K, value: V) = getOrPut(key) { mutableListOf() }.add(value)

    val CharSequence.sql: String
        get() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()

    val CharSequence.reverseUnderScore: String
        get() = replace("_[a-zA-Z]".toRegex()) { match -> match.value[1].toUpperCase().toString() }

    companion object {
        val complexOrmSchema = Class.forName("com.github.silasgermany.complexorm.GeneratedComplexOrmSchema")
            .getDeclaredField("INSTANCE").get(null) as ComplexOrmDatabaseSchemaInterface

        val complexOrmTables =
                Class.forName("com.github.silasgermany.complexorm.GeneratedComplexOrmTables")
                    .getDeclaredField("INSTANCE").get(null) as ComplexOrmTableInfoInterface

        val allTables = complexOrmSchema.tables
    }
}