package com.github.silasgermany.complexormapi

import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import uuid.uuid_generate_md5
import uuid.uuid_generate_random
import uuid.uuid_get_template
import uuid.uuid_t
import kotlin.reflect.KClass

actual class IdType actual constructor(actual val bytes: ByteArray) {

    init {
        if (bytes.size != 16) throw IllegalArgumentException("Wrong byte size (${bytes.size})")
    }

    actual val asSql: String get() = "x'${bytes.asHex}'"

    actual override fun toString(): String {
        val hexChars = bytes.asHex.toCharArray()
        return String(
            hexChars.sliceArray(0..7) + '-' +
                    hexChars.sliceArray(8..11) + '-' +
                    hexChars.sliceArray(12..15) + '-' +
                    hexChars.sliceArray(16..19) + '-' +
                    hexChars.sliceArray(20..31)
        )
    }

    actual override fun equals(other: Any?) =
        (other as? IdType)?.bytes?.contentEquals(bytes) ?: false
    actual override fun hashCode() = bytes.contentHashCode()

    @Suppress("MayBeConstant", "unused")
    actual companion object {
        actual val sqlType = "BLOB"
        actual fun generateRandom() = memScoped {
            val id: uuid_t = allocArray(16)
            uuid_generate_random(id)
            IdType(id.readBytes(16))
        }
        actual fun generateFromString(value: String) = memScoped {
            val id: uuid_t = allocArray(16)
            val namespace = uuid_get_template("oid")
            uuid_generate_md5(id, namespace, value, value.length.convert())
            IdType(id.readBytes(16))
        }
    }

    private val ByteArray.asHex: String get() {
        val hexArray = "0123456789abcdef".toCharArray()
        val hexChars = CharArray(size * 2)
        indices.forEach {
            val lowestByte = this[it].toInt() and 0xFF
            hexChars[it * 2] = hexArray[lowestByte ushr 4]
            hexChars[it * 2 + 1] = hexArray[lowestByte and 0x0F]
        }
        return String(hexChars)
    }
}

actual val KClass<*>.className
    get() = simpleName ?: "class_has_no_name"
