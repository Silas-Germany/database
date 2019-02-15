package com.github.silasgermany.database.tables

open class AppliedMiddleTable(initMap: MutableMap<String, Any?> = default): AllTables.NormalTable(initMap) {
    override var intValue: Int by initMap
}