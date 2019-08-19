package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.*

@ComplexOrmAllTables
interface Model {
    class SchemaTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        @ComplexOrmDefault(false.toString())
        var boolean: Boolean by initMap
        @ComplexOrmDefault(1.toString())
        var int: Int by initMap
        @ComplexOrmDefault(2.toString())
        var long: Long by initMap
        @ComplexOrmDefault(3.1.toString())
        var float: Float by initMap
        @ComplexOrmDefault("testDefault")
        var string: String by initMap
        @ComplexOrmDefault("2019-01-01")
        var date: Date by initMap
        @ComplexOrmDefault(1565762054.toString())
        var dateTime: CommonDateTime by initMap
        var byteArray: ByteArray by initMap

        var nullableEntry: String? by initMap

        var connectedEntry: ReferenceTable? by initMap
        var connectedEntries: List<ReferenceTable> by initMap
    }

    class ReferenceTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        @ComplexOrmReverseConnectedColumn
        var reverseConnectedEntryWithDefaultName: List<TableInfoTable> by initMap
        @ComplexOrmReverseConnectedColumn("connectedEntry")
        var reverseConnectedEntry: List<TableInfoTable> by initMap
        @ComplexOrmReverseJoinColumn("connectedEntries")
        var reverseJoinEntry: List<TableInfoTable> by initMap
        @ComplexOrmSpecialConnectedColumn("special")
        var specialConnectedEntry: TableInfoTable by initMap
    }

    class TableInfoTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        var referenceTable: ReferenceTable by initMap
        var connectedEntry: ReferenceTable by initMap
        var connectedEntries: List<ReferenceTable> by initMap
        var special: String by initMap
    }

    class ReaderTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        var testValue: String by initMap
    }

}