package com.github.silasgermany.database.ui.main

import com.github.silasgermany.complexormapi.SqlReverseConnectedColumn
import com.github.silasgermany.database.sql.OtherTables

interface MainModel {

    class User(initMap: MutableMap<String, Any?> = default): OtherTables.User(initMap) {
        override val name: String? by initMap
        override val spokenLanguages: List<OtherTables.Language> by initMap
        override val interfaceLanguage: OtherTables.Language? by initMap
        @SqlReverseConnectedColumn("")
        val championedLanguages: List<OtherTables.Language> by initMap
    }

/*
    class User(initMap: MutableMap<String, Any?> = default): AllTables.User(initMap) {
        override var id: Int by initMap
        override var name: String by initMap
        override var lastName: String by initMap
        override var firstName: String by initMap
        var notThere: String by initMap
        @SqlReverseConnectedColumn
        override val cities: List<City> by initMap
    }
    class City(initMap: MutableMap<String, Any?> = default): AllTables.City(initMap) {
        override var name: String by initMap
    }
    */
}