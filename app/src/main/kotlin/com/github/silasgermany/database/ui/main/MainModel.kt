package com.github.silasgermany.database.ui.main

import com.github.silasgermany.complexormapi.SqlDefault
import com.github.silasgermany.complexormapi.SqlReverseConnectedColumn
import com.github.silasgermany.database.sql.AllTables
import java.util.*

interface MainModel {

    class Table(initMap: MutableMap<String, Any?> = default) : AllTables.Table1(initMap) {
        override var value1: String by initMap
    }
    class Table1(initMap: MutableMap<String, Any?> = default) : AllTables.Table1(initMap) {
        @SqlDefault("admin")
        override var value1: String by initMap
        override var value2: String? by initMap
        override var value3: Int by initMap
        override var value4: Long? by initMap
        override var value5: ByteArray? by initMap
        override var value6: Float by initMap
        override var value7: Boolean by initMap
        override var value8: Date by initMap
        override var entry1: Table2? by initMap
        override var entries1: List<Table2> by initMap
        @SqlReverseConnectedColumn
        var reverseEntries1: List<Table4> by initMap
        @SqlReverseConnectedColumn("entry1")
        var reverseEntries2: List<Table4> by initMap
    }

    class Table2(initMap: MutableMap<String, Any?> = default) : AllTables.Table2(initMap) {
        override var entries1: List<Table3> by initMap
        override var entries2: List<Table1> by initMap
    }

    class Table3(initMap: MutableMap<String, Any?> = default) : AllTables.Table3(initMap) {
        override var entries1: List<Table4> by initMap
    }

    class Table4(initMap: MutableMap<String, Any?> = default) : AllTables.Table4(initMap) {
        override var id: Long by initMap
        override var entry1: Table1 by initMap
        override var entries1: List<AllTables.Table2> by initMap
        override var table1: Table1? by initMap
    }
}