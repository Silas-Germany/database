package com.github.silasgermany.database.sql

import com.github.silasgermany.complexorm.SqlAllTables
import com.github.silasgermany.complexorm.SqlDefault
import com.github.silasgermany.complexorm.SqlTable

@SqlAllTables
interface AllTables {
    open class User(initMap: MutableMap<String, Any?> = default): SqlTable(initMap) {
        @SqlDefault("admin")
        open val name: String by initMap
        open val firstName: String by initMap
        open val lastName: String by initMap
        open val cities: List<City> by initMap
    }

    open class City(initMap: MutableMap<String, Any?> = default): SqlTable(initMap) {
        open val name: String? by initMap
    }
}