package com.github.silasgermany.database.tables

import com.github.silasgermany.complexormapi.ComplexOrmAllTables
import com.github.silasgermany.complexormapi.ComplexOrmTable


@Suppress("UNUSED")
@ComplexOrmAllTables
interface AllTables {

    open class FirstTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        open val stringValue: String by initMap
        open val intValue: Int by initMap
        open val longValue: Long by initMap
    }

    open class SecondTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap)

    open class NotATable

    open class IndirectTable(initMap: MutableMap<String, Any?> = default): BaseTable(initMap)

    open class DoubleIndirectTable(initMap: MutableMap<String, Any?> = default): MiddleTable(initMap)
}