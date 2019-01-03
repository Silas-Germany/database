package com.github.silasgermany.database.ui.main

import com.github.silasgermany.complexormapi.SqlReverseConnectedColumn
import com.github.silasgermany.database.sql.OtherTables

interface MainModel {

    class Language(initMap: MutableMap<String, Any?> = default): OtherTables.Language(initMap) {
        override var attitude: String? by initMap
        override var name: String by initMap
    }
    class User(initMap: MutableMap<String, Any?> = default): OtherTables.User(initMap) {
        override var name: String? by initMap
        override var spokenLanguages: List<Language> by initMap
        override var interfaceLanguage: Language? by initMap
        @SqlReverseConnectedColumn("champion")
        var championedLanguages: List<Language> by initMap
    }

}