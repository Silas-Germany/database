package com.github.silasgermany.database.sql

import com.github.silasgermany.complexormapi.ComplexOrmReadAlways
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmWriteAlways

open class BaseTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
    @ComplexOrmWriteAlways
    @ComplexOrmReadAlways
    var ownId: Int by initMap
}