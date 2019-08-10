package com.github.silasgermany.complexormapi

import java.util.*
import kotlin.reflect.KClass

// Replace with Int or Long if such an ID should be used
actual typealias IdType = UUID

actual val KClass<*>.className: String get() = java.simpleName.split("$").last()
