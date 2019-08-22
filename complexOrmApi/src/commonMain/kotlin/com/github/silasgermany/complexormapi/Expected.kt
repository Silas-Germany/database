package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

expect class IdType(bytes: ByteArray) {
    val bytes: ByteArray
    // This function should be applicable for SQL (e.g. "'value'" for strings)
    val asSql: String

    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    companion object {
        val sqlType: String
        fun generateRandom(): IdType
        fun generateFromString(value: String): IdType
    }
}

expect val KClass<*>.className: String