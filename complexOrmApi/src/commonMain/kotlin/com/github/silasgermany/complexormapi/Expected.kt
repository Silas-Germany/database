package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

expect class IdType {
    override fun toString(): String
}

expect val KClass<*>.className: String