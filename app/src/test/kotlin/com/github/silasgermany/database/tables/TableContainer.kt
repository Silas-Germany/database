package com.github.silasgermany.database.tables

interface TableContainer {
    interface MiddleTableContainer {
        class ContainedTable(initMap: MutableMap<String, Any?> = default): AllTables.NormalTable(initMap) {
            override var longValue: Long by initMap
        }
    }
}