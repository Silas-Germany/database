package com.github.silasgermany.database.tables

abstract class BaseMiddleTable(initMap: MutableMap<String, Any?> = default): BaseTable() {
    open val middleInheritingValue: Int by initMap
}