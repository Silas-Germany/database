package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.IdType
import kotlin.reflect.KClass

// Classes (typealias in Java)
actual abstract class CommonCursor {
    actual abstract fun isNull(columnIndex: Int): Boolean
    actual abstract fun getInt(columnIndex: Int): Int
    actual abstract fun getLong(columnIndex: Int): Long
    actual abstract fun getFloat(columnIndex: Int): Float
    actual abstract fun getString(columnIndex: Int): String
    actual abstract fun getBlob(columnIndex: Int): ByteArray
    actual fun getId(columnIndex: Int): IdType = getBlob(columnIndex)
}

actual class CommonFile actual constructor(parent: String, child: String) {

    actual constructor(parent: CommonFile, child: String) : this("", "") {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun listFiles(): Array<CommonFile> {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun delete(): Boolean {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun exists(): Boolean {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

actual fun CommonFile.commonReadText(): String {
    null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual fun CommonFile.commonWriteText(text: String) {
}

actual class CommonJSONObject actual constructor(json: String) {
    actual fun keys(): Iterator<String> {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun put(name: String, value: Any): CommonJSONObject {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getJSONObject(name: String): CommonJSONObject {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getString(name: String): String {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual val databaseSchema: com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
    get() = null!!//TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
actual val tableInfo: com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
    get() = null!!//TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

actual fun KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>.isSubClassOf(table: KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>): Boolean {
    null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual val KClass<out com.github.silasgermany.complexormapi.ComplexOrmTable>.longName: String
    get() = null!!//TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS", "unused")
actual class CommonDateTime actual constructor(var1: Long) {
    actual open fun toString(var1: String): String {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual open fun getYear(): Int {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getMonthOfYear(): Int {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getDayOfMonth(): Int {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun plusMonths(var1: Int): CommonDateTime {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getMillis(): Long {
        null!!//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
