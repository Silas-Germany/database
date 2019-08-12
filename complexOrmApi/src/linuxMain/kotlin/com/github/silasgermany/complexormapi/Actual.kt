package com.github.silasgermany.complexormapi

import kotlin.experimental.and
import kotlin.reflect.KClass

actual class IdType(val bytes: ByteArray) {

    init {
        if (bytes.size != 16) throw IllegalArgumentException("Wrong byte size (${bytes.size})")
    }

    actual override fun toString(): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2 + 3)
        hexChars[0] = 'x'
        hexChars[1] = '\''
        hexChars[hexChars.size - 1] = '\''
        bytes.indices.forEach {
            val lowestByte = (bytes[it] and 0xFF.toByte()).toInt()
            hexChars[it * 2 + 2] = hexArray[lowestByte ushr 4]
            hexChars[it * 2 + 3] = hexArray[lowestByte and 0x0F]
        }
        return String(hexChars)
    }
}

actual val KClass<*>.className
    get() = simpleName ?: "class_has_no_name"
