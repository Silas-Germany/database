package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import kotlin.reflect.KClass

internal fun String.toSql() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()

internal val <T: ComplexOrmTable> KClass<T>.tableName: String get() = java.simpleName.toSql()

internal val String.asType get() = ComplexOrmTypes.values().find { it.name == this }!!
