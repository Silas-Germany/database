package com.github.silasgermany.complexorm

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.fprintf
import kotlin.reflect.KClass

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
    get() = this.qualifiedName!!

