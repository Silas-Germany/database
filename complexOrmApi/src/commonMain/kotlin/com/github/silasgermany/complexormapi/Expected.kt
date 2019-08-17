package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

expect class IdType(bytes: ByteArray) {
    override fun toString(): String
    // This function should be applicable for SQL (e.g. "'value'" for strings)
    val asSql: String

}

expect val KClass<*>.className: String