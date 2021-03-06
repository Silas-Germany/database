package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.IdType
import kotlin.reflect.KClass

interface ComplexOrmCursor {
    fun isNull(columnIndex: Int): Boolean
    fun getId(columnIndex: Int): IdType =
        IdType(getBlob(columnIndex))
    fun getBoolean(columnIndex: Int) = getInt(columnIndex) != 0
    fun getInt(columnIndex: Int): Int
    fun getLong(columnIndex: Int): Long
    fun getFloat(columnIndex: Int): Float
    fun getString(columnIndex: Int): String
    fun getDate(columnIndex: Int) = Date(getString(columnIndex))
    fun getDateTime(columnIndex: Int) =
        CommonDateTime(getInt(columnIndex) * 1000L)
    fun getBlob(columnIndex: Int): ByteArray
    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    fun <T: Any>get(columnIndex: Int, returnClass: KClass<T>): T? =
        if (isNull(columnIndex)) null
        else when (returnClass) {
            IdType::class -> getId(columnIndex)
            Boolean::class -> getBoolean(columnIndex)
            Int::class -> getInt(columnIndex)
            Long::class -> getLong(columnIndex)
            Float::class -> getFloat(columnIndex)
            String::class -> getString(columnIndex)
            Date::class -> getDate(columnIndex)
            CommonDateTime::class -> getDateTime(columnIndex)
            ByteArray::class -> getBlob(columnIndex)
            else -> throw IllegalArgumentException("Unknown column class: $returnClass")
        } as T
}