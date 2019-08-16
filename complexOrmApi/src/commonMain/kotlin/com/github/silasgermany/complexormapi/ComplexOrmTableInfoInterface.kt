package com.github.silasgermany.complexormapi

interface ComplexOrmTableInfoInterface {
    val normalColumns: Map<String, Map<String, String>>
    val connectedColumns: Map<String, Map<String, String>>
    val joinColumns: Map<String, Map<String, String>>
    val reverseJoinColumns: Map<String, Map<String, String>>
    val reverseConnectedColumns: Map<String, Map<String, String>>
    val specialConnectedColumns: Map<String, Map<String, String>>
    val enumColumns: Map<String, Map<String, List<Enum<*>>>>
    val tableConstructors: Map<String, (Map<String, Any?>) -> ComplexOrmTable>
    val basicTableInfo: Map<String, Pair<String, String>>
    val columnNames: Map<String, Map<String, String>>
}