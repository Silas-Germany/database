package com.github.silasgermany.database.tables

import com.github.silasgermany.complexormapi.ComplexOrmTable

abstract class BaseTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
    open val inheritingValue: Int by initMap
}
