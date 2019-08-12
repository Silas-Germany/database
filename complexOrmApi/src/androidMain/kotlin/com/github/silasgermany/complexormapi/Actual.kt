package com.github.silasgermany.complexormapi

import java.nio.ByteBuffer
import java.util.*
import kotlin.reflect.KClass

// Replace with Int or Long if such an ID should be used
actual class IdType(private val uuid: UUID) {
    constructor(bytes: ByteArray) : this(ByteBuffer.wrap(bytes).run { UUID(long, long) })
    val bytes: ByteArray = uuid.run {
        ByteBuffer.allocate(2 * Long.SIZE_BYTES)
            .putLong(mostSignificantBits)
            .putLong(leastSignificantBits)
            .array()
    }
    actual override fun toString() = "x'${uuid.toString().replace("-", "")}'"
}

actual val KClass<*>.className: String get() = java.simpleName//.split("$").last()
val KClass<*>.className2: String get() = java.canonicalName//.split("$").last()
