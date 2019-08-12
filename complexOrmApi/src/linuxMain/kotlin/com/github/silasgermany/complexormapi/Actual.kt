package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

actual typealias IdType = ByteArray

actual val KClass<*>.className
    get() = simpleName ?: "class_has_no_name"