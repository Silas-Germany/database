package com.github.silasgermany.complexormapi

import java.nio.ByteBuffer
import java.util.*
import kotlin.reflect.KClass

// Replace with Int or Long if such an ID should be used
actual class IdType(private val uuid: UUID) {

    actual constructor(bytes: ByteArray) : this(ByteBuffer.wrap(bytes).run { UUID(long, long) }) {
        if (bytes.size != 16) throw IllegalArgumentException("Wrong byte size (${bytes.size})")
    }

    actual val bytes: ByteArray get() = uuid.run {
        ByteBuffer.allocate(2 * Long.SIZE_BYTES)
            .putLong(mostSignificantBits)
            .putLong(leastSignificantBits)
            .array()
    }

    actual val asSql get() = "x'${uuid.toString().replace("-", "")}'"

    actual override fun toString(): String = "$uuid"
    actual override fun equals(other: Any?) = uuid == (other as? IdType)?.uuid
    actual override fun hashCode() = uuid.hashCode()

    actual companion object {
        actual val sqlType = "BLOB"
        actual fun generateRandom() = IdType(UUID.randomUUID())
        actual fun generateFromString(value: String) = IdType(UUID.nameUUIDFromBytes(value.toByteArray()))
        actual fun decode(bytes: ByteArray): String = bytes.run {
            val hexArray = "0123456789abcdef".toCharArray()
            val hexChars = CharArray(size * 2)
            indices.forEach {
                val lowestByte = this[it].toInt() and 0xFF
                hexChars[it * 2] = hexArray[lowestByte ushr 4]
                hexChars[it * 2 + 1] = hexArray[lowestByte and 0x0F]
            }
            return String(hexChars)
        }

        actual fun encode(string: String) =
            ByteArray(16) {
                (Character.digit(string[it], 16).shl(4) +
                        Character.digit(string[it], 16)).toByte()
            }
    }
}

actual val KClass<*>.className: String get() = java.simpleName.split('$').last()
