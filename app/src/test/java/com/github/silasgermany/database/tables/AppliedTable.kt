package com.github.silasgermany.database.tables

class AppliedTable(initMap: MutableMap<String, Any?> = default): AppliedMiddleTable(initMap) {
    override var longValue: Long by initMap
}