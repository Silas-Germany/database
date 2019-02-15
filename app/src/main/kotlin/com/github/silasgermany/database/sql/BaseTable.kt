package com.github.silasgermany.database.sql

import com.github.silasgermany.complexormapi.ComplexOrmReadAlways
import com.github.silasgermany.complexormapi.ComplexOrmTable

open class BaseTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
    @ComplexOrmReadAlways
    var ownId: Int by initMap
}