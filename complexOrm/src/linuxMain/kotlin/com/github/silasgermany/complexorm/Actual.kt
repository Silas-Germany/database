package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.IdType
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.reflect.KClass

// Classes (typealias in Java)
actual abstract class CommonCursor {
    actual abstract fun isNull(columnIndex: Int): Boolean
    actual abstract fun getInt(columnIndex: Int): Int
    actual abstract fun getLong(columnIndex: Int): Long
    actual abstract fun getFloat(columnIndex: Int): Float
    actual abstract fun getString(columnIndex: Int): String
    actual abstract fun getBlob(columnIndex: Int): ByteArray
    actual fun getId(columnIndex: Int): IdType = IdType(getBlob(columnIndex))
}

@Suppress("unused")
actual class CommonFile actual constructor(private val parent: String, private val child: String) {

    actual fun getPath() = "$parent/$child"

    actual fun listFiles(): Array<CommonFile> {
        val dir = opendir(getPath())
        val files = mutableListOf<CommonFile>()
        while(true) {
            val file = readdir(dir) ?: break
            files.add(CommonFile(getPath(), file.pointed.d_name.toKString()))
        }
        closedir(dir)
        return files.toTypedArray()
    }

    actual fun delete() = remove(getPath()) == 0

    actual fun exists() = access(getPath(), F_OK) != -1

}

actual fun CommonFile.commonReadText(): String {
    val file = fopen(getPath(), "r") ?: throw IllegalStateException("Can't open the file")
    val result = StringBuilder()
    memScoped {
        val bufferSize = 100 * 1024
        val content = allocArray<ByteVar>(bufferSize)
        while (true) {
            fgets(content, bufferSize, file) ?: break
            result.append(content.toKString())
        }
    }
    fclose(file)
    return result.toString()
}

actual fun CommonFile.commonWriteText(text: String) {
    val file = fopen(getPath(), "w") ?: throw IllegalStateException("Can't open the file")
    fprintf(file, text)
    fclose(file)
}

actual val KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>.longName: String
    get() = toString()

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
            strftime(result, neededResultSize.convert(), formatString, localtime(cValuesOf(epochTime)))
            result.toKString()
        }
    }
}
