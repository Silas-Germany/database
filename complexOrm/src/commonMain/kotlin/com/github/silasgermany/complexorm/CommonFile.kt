package com.github.silasgermany.complexorm

expect class CommonFile(parent: String, child: String) {
    constructor(pathname: String)
    constructor(parent: CommonFile, child: String)

    fun getPath(): String
    fun listFiles(): Array<CommonFile>?
    fun delete(): Boolean
    fun exists(): Boolean
    fun mkdir(): Boolean
    fun getParentFile(): CommonFile
}