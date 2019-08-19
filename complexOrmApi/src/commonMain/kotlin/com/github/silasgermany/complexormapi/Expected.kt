package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

expect class IdType(bytes: ByteArray) {
    // This function should be applicable for SQL (e.g. "'value'" for strings)
    val asSql: String

    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

expect val KClass<*>.className: String