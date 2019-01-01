package com.github.silasgermany.database.sql

import com.github.silasgermany.complexorm.SqlTable

interface AllTables {
    open class User(initMap: MutableMap<String, Any?> = default): SqlTable(initMap) {
    }
}