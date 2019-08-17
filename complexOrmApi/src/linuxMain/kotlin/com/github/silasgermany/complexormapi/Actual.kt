package com.github.silasgermany.complexormapi

import kotlin.reflect.KClass

actual class IdType actual constructor(val bytes: ByteArray) {

    init {
        if (bytes.size != 16) throw IllegalArgumentException("Wrong byte size (${bytes.size})")
    }

    actual override fun toString(): String {
        val hexArray = "0123456789abcdef".toCharArray()
        val hexChars = CharArray(16 * 2)
        (0..15).forEach {
            val lowestByte = bytes[it].toInt() and 0xFF
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

    actual val asSql: String get() {
        val hexArray = "0123456789abcdef".toCharArray()
        val hexChars = try {
        val hexChars = CharArray(16 * 2 + 3)
        hexChars[0] = 'x'
        hexChars[1] = '\''
        hexChars[15 * 2 + 4] = '\''
            hexChars
        } catch (e: Throwable) { println(1); throw e}
        (0..15).forEach {
            val lowestByte = bytes[it].toInt() and 0xFF
            hexChars[it * 2 + 2] = hexArray[lowestByte ushr 4]
            hexChars[it * 2 + 3] = hexArray[lowestByte and 0x0F]
        }
        return String(hexChars)
    }
}

actual val KClass<*>.className
    get() = simpleName ?: "class_has_no_name"
