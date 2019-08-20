package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.*
import kotlin.reflect.KClass

// Classes (typealias in Java)
interface ComplexOrmCursor {
    fun isNull(columnIndex: Int): Boolean
    fun getId(columnIndex: Int): IdType = IdType(getBlob(columnIndex))
    fun getBoolean(columnIndex: Int) = getInt(columnIndex) != 0
    fun getInt(columnIndex: Int): Int
    fun getLong(columnIndex: Int): Long
    fun getFloat(columnIndex: Int): Float
    fun getString(columnIndex: Int): String
    fun getDate(columnIndex: Int) = Date(getString(columnIndex))
    fun getDateTime(columnIndex: Int) =
        CommonDateTime(getInt(columnIndex) * 1000L)
    fun getBlob(columnIndex: Int): ByteArray
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