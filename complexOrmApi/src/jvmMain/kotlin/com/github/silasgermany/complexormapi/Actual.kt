package com.github.silasgermany.complexormapi

import java.util.*
import kotlin.reflect.KClass

actual typealias CommonUUID = UUID
actual object CommonUUIDObject {
    actual fun randomCommonUUID(): CommonUUID = UUID.randomUUID()

    actual fun nameUUIDFromBytes(name: String): CommonUUID = UUID.nameUUIDFromBytes(name.toByteArray())
}

actual val KClass<*>.shortName: String get() = java.simpleName