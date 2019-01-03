package com.github.silasgermany.database.ui.main

import com.github.silasgermany.complexormapi.SqlReverseConnectedColumn
import com.github.silasgermany.database.sql.OtherTables

interface MainModel {

    class User(initMap: MutableMap<String, Any?> = default): OtherTables.User(initMap) {
        override val name: String? by initMap
        override val spokenLanguages: List<OtherTables.Language> by initMap
        override val interfaceLanguage: OtherTables.Language? by initMap
        @SqlReverseConnectedColumn("champion")
        val championedLanguages: List<OtherTables.Language> by initMap
    }
}