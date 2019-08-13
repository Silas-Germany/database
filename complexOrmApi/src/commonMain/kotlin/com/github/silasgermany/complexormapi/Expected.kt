package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

expect class IdType {
    fun nicePrint(): String
    // This function should be applicable for SQL (e.g. "'value'" for strings)
    override fun toString(): String
}

expect val KClass<*>.className: String