package com.github.silasgermany.database.tables

import java.util.*

interface AppliedTablesInterface {

    class NormalTable(initMap: MutableMap<String, Any?> = default): AllTables.NormalTable(initMap) {
        override var longValue: Long by initMap
        override val dateValue: Date by initMap
        override var byteArrayValue: ByteArray by initMap
    }

    open class NormalTableWithWritableIntValue(initMap: MutableMap<String, Any?> = default): AllTables.NormalTable(initMap) {
        override var intValue: Int by initMap
    }

    class NormalTableWithoutWritableIntValue(initMap: MutableMap<String, Any?> = default): NormalTableWithWritableIntValue(initMap) {
        override var stringValue: String by initMap
    }
}