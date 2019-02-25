package com.github.silasgermany.complexormapi

import java.util.*

interface ComplexOrmTableInfoInterface {
    val normalColumns: SortedMap<String, SortedMap<String, ComplexOrmTypes>>
    val connectedColumns: SortedMap<String, SortedMap<String, String>>
    val joinColumns: SortedMap<String, SortedMap<String, String>>
    val reverseJoinColumns: SortedMap<String, SortedMap<String, Pair<String, String>>>
    val reverseConnectedColumns: SortedMap<String, SortedMap<String, Pair<String, String>>>
    val specialConnectedColumns: SortedMap<String, SortedMap<String, Pair<String, String>>>
    val tableConstructors: SortedMap<String, (Map<String, Any?>) -> ComplexOrmTable>
    val basicTableInfo: SortedMap<String, Pair<String, String>>
    val columnNames: SortedMap<String, SortedMap<String, String>>
}