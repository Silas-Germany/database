package com.github.silasgermany.database.sql

import com.github.silasgermany.complexormapi.ComplexOrmReadAlways
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.UUID

open class BaseTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
    @ComplexOrmReadAlways
    var ownId: UUID by initMap
}