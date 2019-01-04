package com.github.silasgermany.database.sql

import com.github.silasgermany.complexormapi.SqlAllTables
import com.github.silasgermany.complexormapi.SqlDefault
import com.github.silasgermany.complexormapi.SqlTable
import java.util.*


@Suppress("UNUSED")
@SqlAllTables
interface AllTables {

    open class Table1(initMap: MutableMap<String, Any?> = default): SqlTable(initMap) {
        @SqlDefault("admin")
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

    open class Table2(initMap: MutableMap<String, Any?> = default): SqlTable(initMap) {
        open val entries1: List<Table3> by initMap
        open val entries2: List<Table1> by initMap
    }

    open class Table3(initMap: MutableMap<String, Any?> = default): SqlTable(initMap) {
        open val entries1: List<Table4> by initMap
    }

    open class Table4(initMap: MutableMap<String, Any?> = default): SqlTable(initMap) {
        open val entry1: Table1? by initMap
        open val entries1: List<Table2> by initMap
        open val table1: Table1? by initMap
    }
}