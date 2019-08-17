package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.IdType
import kotlin.reflect.KClass

// Classes (typealias in Java)
interface CommonCursor {
    fun isNull(columnIndex: Int): Boolean
    fun getInt(columnIndex: Int): Int
    fun getLong(columnIndex: Int): Long
    fun getFloat(columnIndex: Int): Float
    fun getString(columnIndex: Int): String
    fun getBlob(columnIndex: Int): ByteArray
    fun getId(columnIndex: Int): IdType = IdType(getBlob(columnIndex))
}

expect class CommonFile constructor(parent: String, child: String) {
    fun getPath(): String
    fun listFiles(): Array<CommonFile>
    fun delete(): Boolean
    fun exists(): Boolean
}
expect fun CommonFile.commonReadText(): String
expect fun CommonFile.commonWriteText(text: String)
@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS")
expect class CommonDateTime(var1: Long) {

    open fun getYear(): Int
    fun getMonthOfYear(): Int
    fun getDayOfMonth(): Int
    fun getMillis(): Long

    fun plusMonths(var1: Int): CommonDateTime
    open fun toString(var1: String): String
}

// Get generated classes
expect val databaseSchema: ComplexOrmDatabaseSchemaInterface
expect val tableInfo: ComplexOrmTableInfoInterface
expect val KClass<out ComplexOrmTable>.longName: String
val ComplexOrmTable.longName get() = this::class.longName