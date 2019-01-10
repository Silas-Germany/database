package com.github.silasgermany.database.sql

import com.github.silasgermany.complexormapi.ComplexOrmAllTables
import com.github.silasgermany.complexormapi.ComplexOrmDefault
import com.github.silasgermany.complexormapi.ComplexOrmTable
import java.util.*


@Suppress("UNUSED")
@ComplexOrmAllTables
interface AllTables {

    open class Table1(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        @ComplexOrmDefault("admin")
        open val value1: String? by initMap
        open val value2: String? by initMap
        open val value3: Int? by initMap
        open val value4: Long? by initMap
        open val value5: ByteArray? by initMap
        open val value6: Float? by initMap
        open val value7: Boolean? by initMap
        open val value8: Date? by initMap
        open val entry1: Table2? by initMap
        open val entries1: List<Table2> by initMap
    }

    open class Table2(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        open val entries1: List<Table3> by initMap
        open val entries2: List<Table1> by initMap
    }

    open class Table3(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        open val entries1: List<Table4> by initMap
    }

    open class Table4(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        open val entry1: Table1? by initMap
        open val entries1: List<Table2> by initMap
        open val table1: Table1? by initMap
    }

    open class SpecialTable(initMap: MutableMap<String, Any?>): BaseTable(initMap) {
        open val value: String by initMap
        open val noValue: String = ""
        open val getValue: () -> String by initMap
        fun getValue(): Int {
            ownId = 1
            return ownId
        }
    }
}