package com.github.silasgermany.database.tables

import com.github.silasgermany.complexormapi.ComplexOrmReadAlways

abstract class BaseMiddleTable(initMap: MutableMap<String, Any?> = default): BaseTable() {
    @ComplexOrmReadAlways
    open val middleInheritingValue: Int by initMap
}