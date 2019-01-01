package com.github.silasgermany.database.ui.main

import com.github.silasgermany.database.sql.AllTables

interface MainModel {
    class User(initMap: MutableMap<String, Any?> = default): AllTables.User(initMap) {
        val name: String by initMap
    }
}