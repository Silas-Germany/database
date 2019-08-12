package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

expect class IdType {
    // This function should be applicable for SQL (e.g. "'value'" for strings)
    override fun toString(): String
}

expect val KClass<*>.className: String