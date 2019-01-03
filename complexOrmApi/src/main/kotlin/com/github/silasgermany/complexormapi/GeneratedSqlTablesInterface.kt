package com.github.silasgermany.complexormapi

interface GeneratedSqlTablesInterface {
    val constructors: Map<String, Map<String, (Map<String, Any?>) -> SqlTable>>
    val normalColumns: Map<String, Map<String, Map<String, SqlTypes>>>
    val joinColumns: Map<String, Map<String, Map<String, String>>>
    val connectedColumns: Map<String, Map<String, Map<String, String?>>>
    val reverseConnectedColumns: Map<String, Map<String, Map<String, Pair<String, String?>>>>

}