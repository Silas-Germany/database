package com.github.silasgermany.database.ui.main

import com.github.silasgermany.complexorm.SqlTablesInterface
import com.github.silasgermany.database.sql.AllTables

@SqlTablesInterface
interface MainModel {
    class User(initMap: MutableMap<String, Any?> = default): AllTables.User(initMap) {
        val name: String by initMap
    }
}