package com.github.silasgermany.database.ui.main

import com.github.silasgermany.complexorm.SqlReverseConnectedColumn
import com.github.silasgermany.database.sql.AllTables

interface MainModel {
    class User(initMap: MutableMap<String, Any?> = default): AllTables.User(initMap) {
        override var name: String by initMap
        override var lastName: String by initMap
        override var firstName: String by initMap
        @SqlReverseConnectedColumn
        override val cities: List<City> by initMap
    }
    class City(initMap: MutableMap<String, Any?> = default): AllTables.City(initMap) {
        override var name: String by initMap
    }
}