package com.github.silasgermany.database.sql

import com.github.silasgermany.complexorm.SqlAllTables
import com.github.silasgermany.complexorm.SqlDefault
import com.github.silasgermany.complexorm.SqlIgnore
import com.github.silasgermany.complexorm.SqlTable

@SqlAllTables
interface AllTables {
    open class User(initMap: MutableMap<String, Any?> = default): SqlTable(initMap) {
        @SqlDefault("admin")
        open val name: String by initMap
        @SqlIgnore
        open val address: Long by initMap
        open fun addressx() = 1
    }

    open class City(initMap: MutableMap<String, Any?> = default): SqlTable(initMap) {
        open val name: String? by initMap
    }
}