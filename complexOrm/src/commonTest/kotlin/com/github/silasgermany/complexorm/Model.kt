package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.*

@ComplexOrmAllTables
interface Model {
    class SchemaTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        @ComplexOrmDefault(false.toString())
        val boolean: Boolean by initMap
        @ComplexOrmDefault(1.toString())
        val int: Int by initMap
        @ComplexOrmDefault(2.toString())
        val long: Long by initMap
        @ComplexOrmDefault(3.1.toString())
        val float: Float by initMap
        @ComplexOrmDefault("testDefault")
        val string: String by initMap
        @ComplexOrmDefault("2019-01-01")
        val date: Date by initMap
        @ComplexOrmDefault(1565762054.toString())
        val dateTime: CommonDateTime by initMap
        val byteArray: ByteArray by initMap

        val nullableEntry: String? by initMap

        val connectedEntry: ReferenceTable by initMap
        val connectedEntries: List<ReferenceTable> by initMap
    }

    class ReferenceTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        @ComplexOrmReverseConnectedColumn
        val reverseConnectedEntryWithDefaultName: List<TableInfoTable> by initMap
        @ComplexOrmReverseConnectedColumn("connectedEntry")
        val reverseConnectedEntry: List<TableInfoTable> by initMap
        @ComplexOrmReverseJoinColumn("connectedEntries")
        val reverseJoinEntry: List<TableInfoTable> by initMap
        @ComplexOrmSpecialConnectedColumn("specialId")
        val specialConnectedEntry: TableInfoTable by initMap
    }

    class TableInfoTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        val referenceTable: ReferenceTable by initMap
        val connectedEntry: ReferenceTable by initMap
        val connectedEntries: List<ReferenceTable> by initMap
        val specialId: String by initMap
    }

}