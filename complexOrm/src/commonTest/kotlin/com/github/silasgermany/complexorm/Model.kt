package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmAllTables
import com.github.silasgermany.complexormapi.ComplexOrmTable

@ComplexOrmAllTables
interface Model {
    class Test(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        val string2: String by initMap
        val nullableString: String? by initMap
    }
}