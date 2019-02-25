package com.github.silasgermany.complexormapi

import java.util.*

interface ComplexOrmTableInfoInterface {
    val normalColumns: SortedMap<String, Map<String, String>>
    val connectedColumns: SortedMap<String, Map<String, String>>
    val joinColumns: SortedMap<String, Map<String, String>>
    val reverseJoinColumns: SortedMap<String, Map<String, String>>
    val reverseConnectedColumns: SortedMap<String, Map<String, String>>
    val specialConnectedColumns: SortedMap<String, Map<String, String>>
    val tableConstructors: SortedMap<String, (Map<String, Any?>) -> ComplexOrmTable>
    val basicTableInfo: SortedMap<String, Pair<String, String>>
    val columnNames: SortedMap<String, Map<String, String>>
}