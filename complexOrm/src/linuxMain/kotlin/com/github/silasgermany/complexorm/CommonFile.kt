package com.github.silasgermany.complexorm

import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.*

@Suppress("unused")
actual class CommonFile actual constructor(private val pathname: String) {

    actual constructor(parent: String, child: String) : this(if (parent.endsWith('/')) "$parent$child" else "$parent/$child")
    actual constructor(parent: CommonFile, child: String) : this(parent.getPath(), child)
    actual fun getPath() = pathname

    actual fun listFiles(): Array<CommonFile>? {
        if (!exists()) return null
        val dir = opendir(getPath())
        val files = mutableListOf<CommonFile>()
        while(true) {
            val file = readdir(dir) ?: break
            when (val fileName = file.pointed.d_name.toKString()) {
                "..", "." -> {}
                else -> files.add(CommonFile(getPath(), fileName))
            }
        }
        closedir(dir)
        return files.takeUnless { it.isEmpty() }?.toTypedArray()
    }

    actual fun delete() = remove(getPath()) == 0

    actual fun exists() = access(getPath(), F_OK) != -1

    actual fun mkdir() = mkdir(pathname, 511.convert()) == 0

    actual fun getParentFile(): CommonFile =
        CommonFile(dirname(pathname.cstr)!!.toKString())

}