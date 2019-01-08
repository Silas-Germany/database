package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.GeneratedSqlSchemaInterface
import com.github.silasgermany.complexormapi.SqlTable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.jvmErasure

abstract class SqlUtils {

    private val sqlSchema = Class.forName("com.github.silasgermany.complexorm.GeneratedSqlSchema")
        .getDeclaredField("INSTANCE").get(null) as GeneratedSqlSchemaInterface

    inline val <reified T : SqlTable> KProperty1<T, *>.fullName get() = "${T::class.tableName}.$columnName"

    val KProperty1<out SqlTable, *>.columnName get() =
        if (sqlSchema.tableNames.contains(returnType.jvmErasure.simpleName!!.sql)) "${name}_id"
        else name

    val KClass<out SqlTable>.tableName get() = simpleName!!.sql.takeIf { sqlSchema.tableNames.contains(it) }
        ?: superclasses.find { sqlSchema.tableNames.contains(it.simpleName?.sql) }?.simpleName?.sql!!


    fun <K, V> MutableMap<K, MutableList<V>>.add(key: K, value: V) = getOrPut(key) { mutableListOf() }.add(value)

    val CharSequence.sql: String
        get() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()

    val CharSequence.reverseUnderScore: String
        get() = replace("_[a-zA-Z]".toRegex()) { match -> match.value[1].toUpperCase().toString() }
}