package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmAllTables
import com.github.silasgermany.complexormapi.ComplexOrmDefault
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.Date

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

        val connectedEntry: ReferenceClass by initMap
        val connectedEntries: List<ReferenceClass> by initMap
    }
    class ReferenceClass(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap)
}