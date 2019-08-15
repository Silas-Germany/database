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

actual class CommonFile actual constructor(private val parent: String, private val child: String) {

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

    actual fun getPath() = "$parent/$child"

}

@ExperimentalUnsignedTypes
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

actual val databaseSchema: com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
    get() = null!!//ComplexOrmDatabaseSchema()
actual val tableInfo: com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
    get() = null!!//ComplexOrmTableInfo()

actual val KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>.longName: String
    get() = toString()

@ExperimentalUnsignedTypes
@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS", "unused")
actual class CommonDateTime actual constructor(var1: Long) {
    private val epochTime: time_t = var1
    actual open fun toString(var1: String) =
        memScoped {
            val result = allocArray<ByteVar>(var1.length)
            strftime(result, var1.length.toULong(), var1, localtime(epochTime.toCPointer()))
            result.toKString()
        }

    actual open fun getYear() =
        localtime(epochTime.toCPointer())!!.pointed.tm_year + 1900

    actual fun getMonthOfYear() =
        localtime(epochTime.toCPointer())!!.pointed.tm_mon

    actual fun getDayOfMonth() =
        localtime(epochTime.toCPointer())!!.pointed.tm_mday

    actual fun plusMonths(var1: Int): CommonDateTime {
        val time = localtime(epochTime.toCPointer())!!
        time.pointed.tm_mon += 1
        return CommonDateTime(mktime(time))
    }

    actual fun getMillis(): Long = epochTime

}
