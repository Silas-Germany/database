package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.*

@ComplexOrmAllTables
interface Model {
    class ColumnTypesTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
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

    class SimpleTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        var testValue: String? by initMap
        var connectedEntry: SimpleReferenceTable? by initMap
    }

    class SimpleReferenceTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        var anotherSimpleEntry: SimpleTable by initMap
    }

    class AdvancedTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        var normalEntries: List<AdvancedReferenceTable> by initMap
        @ComplexOrmReverseJoinColumn("advancedEntries")
        var reverseJoinEntries: List<AdvancedReferenceTable> by initMap
        @ComplexOrmReverseConnectedColumn()
        var reverseNormalEntries: List<AdvancedReferenceTable> by initMap
        @ComplexOrmReverseConnectedColumn("specialAdvancedEntry")
        var reverseSpecialEntries: List<AdvancedReferenceTable> by initMap
        @ComplexOrmSpecialConnectedColumn("special")
        var specialConnectedEntry: AdvancedReferenceTable? by initMap
    }

    class AdvancedReferenceTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        var advancedEntries: List<AdvancedTable> by initMap
        var advancedTable: AdvancedTable? by initMap
        var specialAdvancedEntry: AdvancedTable? by initMap
        var special: IdType? by initMap
    }

    class PropertiesTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
        @ComplexOrmUnique
        var uniqueEntry: String? by initMap
        @ComplexOrmUniqueIndex
        var doubleUniqueEntry1: String? by initMap
        @ComplexOrmUniqueIndex
        var doubleUniqueEntry2: String? by initMap
        @ComplexOrmUniqueIndex(1)
        var tripleUniqueEntry1: String? by initMap
        @ComplexOrmUniqueIndex(1)
        var tripleUniqueEntry2: String? by initMap
        @ComplexOrmUniqueIndex(1)
        var tripleUniqueEntry3: String? by initMap
        @ComplexOrmProperty("CHECK(LENGTH(maxLengthEntry) <= 3)")
        var maxLengthEntry: String? by initMap
        var defaultSetNullRestriction: PropertiesReferenceTable? by initMap
        var defaultDeleteRestriction: PropertiesReferenceTable by initMap
        @ComplexOrmDeleteRestrict
        var deleteRestrictEntry: PropertiesReferenceTable? by initMap
        @ComplexOrmDeleteCascade
        var deleteCascadeEntry: PropertiesReferenceTable? by initMap
    }
    
    class PropertiesReferenceTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap)
}