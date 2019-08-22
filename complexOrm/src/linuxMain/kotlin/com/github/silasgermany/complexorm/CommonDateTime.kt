package com.github.silasgermany.complexorm

import kotlinx.cinterop.*
import platform.posix.localtime
import platform.posix.mktime
import platform.posix.strftime
import platform.posix.time_t

@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS", "unused", "unused")
actual class CommonDateTime actual constructor(var1: Long) {

    private val epochTime: time_t = var1 / 1000

    actual open fun getYear() =
        localtime(cValuesOf(epochTime))!!.pointed.tm_year + 1900

    actual fun getMonthOfYear() =
        localtime(cValuesOf(epochTime))!!.pointed.tm_mon + 1

    actual fun getDayOfMonth() =
        localtime(cValuesOf(epochTime))!!.pointed.tm_mday

    actual fun getMillis(): Long = epochTime * 1000

    actual fun plusMonths(var1: Int): CommonDateTime {
        val time = localtime(cValuesOf(epochTime))!!
        time.pointed.tm_mon += 1
        return CommonDateTime(mktime(time))
    }

    actual open fun toString(var1: String): String {
        val formatMap = mapOf(
            "EEEEE" to "%A",
            "MMMMM" to "%B",
            "yyyy" to "%Y",
            "MM" to "%m",
            "dd" to "%d",
            "hh" to "%I",
            "mm" to "%M",
            "ss" to "%S",
            "HH" to "%H",
            "E" to "%a"
            )
        val formatString = formatMap.toList().fold(var1) { format, (old, new) -> format.replace(old, new) }
        if (formatString.matches(".*(?!%).[A-Za-z].*".toRegex())) {
            throw IllegalArgumentException("Format should only contain: ${formatMap.keys}. It is: '$var1' (-> $formatString)")
        }
        return memScoped {
            val neededResultSize = var1.length + 30
            val result = allocArray<ByteVar>(neededResultSize)
            strftime(
                result,
                neededResultSize.convert(),
                formatString,
                localtime(cValuesOf(epochTime))
            )
            result.toKString()
        }
    }
}