package com.github.silasgermany.complexormapi

import kotlin.experimental.and
import kotlin.reflect.KClass

actual class IdType(val bytes: ByteArray) {

    init {
        if (bytes.size != 16) throw IllegalArgumentException("Wrong byte size (${bytes.size})")
    }

    actual fun nicePrint(): String {
        val hexArray = "0123456789abcdef".toCharArray()
        val hexChars = CharArray(16 * 2)
        bytes.indices.forEach {
            val lowestByte = bytes[it].toInt() and 0xFF
            println(lowestByte ushr 4)
            println(lowestByte and 0x0F)
            hexChars[it * 2] = hexArray[lowestByte ushr 4]
            hexChars[it * 2 + 1] = hexArray[lowestByte and 0x0F]
        }
        return String(
            hexChars.sliceArray(0..7) + '-' +
            hexChars.sliceArray(8..11) + '-' +
            hexChars.sliceArray(12..15) + '-' +
            hexChars.sliceArray(16..19) + '-' +
            hexChars.sliceArray(20..31)
        )
    }

    actual override fun toString(): String {
        val hexArray = "0123456789abcdef".toCharArray()
        val hexChars = CharArray(16 * 2 + 3)
        hexChars[0] = 'x'
        hexChars[1] = '\''
        hexChars[16 * 2 + 2] = '\''
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
