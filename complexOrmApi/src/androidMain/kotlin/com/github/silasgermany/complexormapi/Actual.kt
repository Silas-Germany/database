package com.github.silasgermany.complexormapi

import java.nio.ByteBuffer
import java.util.*
import kotlin.reflect.KClass

// Replace with Int or Long if such an ID should be used
actual class IdType(private val uuid: UUID) {

    actual constructor(bytes: ByteArray) : this(ByteBuffer.wrap(bytes).run { UUID(long, long) }) {
        if (bytes.size != 16) throw IllegalArgumentException("Wrong byte size (${bytes.size})")
    }

    val bytes: ByteArray get() = uuid.run {
        ByteBuffer.allocate(2 * Long.SIZE_BYTES)
            .putLong(mostSignificantBits)
            .putLong(leastSignificantBits)
            .array()
    }

    actual override fun toString(): String = "$uuid"
    actual val asSql get() = "x'${uuid.toString().replace("-", "")}'"
}

actual val KClass<*>.className: String get() = java.simpleName
