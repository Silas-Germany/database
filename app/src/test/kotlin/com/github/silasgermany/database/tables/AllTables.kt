package com.github.silasgermany.database.tables

import com.github.silasgermany.complexormapi.*
import java.util.*


@Suppress("UNUSED")
@ComplexOrmAllTables
interface AllTables {

    open class NormalTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        constructor(id: Long): this(init(id))
        fun clone(booleanValue: Boolean = true) = cloneWithoutId("booleanValue" to booleanValue)

        @ComplexOrmReadAlways
        @ComplexOrmDefault("${true}")
        open var booleanValue: Boolean by initMap

        @ComplexOrmDefault("${1}")
        open val intValue: Int? by initMap
        @ComplexOrmUnique
        @ComplexOrmProperty("CHECK(LENGTH(stringValue) = 3)")
        @ComplexOrmDefault("123")
        open var stringValue: String by initMap
        @ComplexOrmDefault("${1L}")
        open val longValue: Long by initMap
        @ComplexOrmDefault("${1F}")
        open var floatValue: Float by initMap
        //@ComplexOrmDefault("31/12/2018")
        open val dateValue: Date by initMap
        open val byteArrayValue: ByteArray? by initMap

        open val otherTableValue: EmptyTable? by initMap
        open var otherTableValues: List<EmptyTable> by initMap

        @ComplexOrmReverseConnectedColumn("normalTableValue")
        open var connectedTableValues: List<ReferenceTable> by initMap
        @ComplexOrmReverseConnectedColumn("normal_table_value")
        open var otherWritingConnectedTableValues: List<ReferenceTable> by initMap
        @ComplexOrmReverseConnectedColumn
        open var columnEqualsTableNameConnectedTableValues: List<ReferenceTable> by initMap
        @ComplexOrmReverseJoinColumn("normalTableValues")
        open var joinTableValues: List<ReferenceTable> by initMap
        @ComplexOrmReverseJoinColumn("normal_table_values")
        open var otherWritingJoinTableValues: List<ReferenceTable> by initMap

        // No map delegations
        open val getIntValue: () -> Int = {1}
        open val getStringValue: String = ""
        open val getLongValue: Long = 1L
        open val notValidOtherClassValue: NotATable? = null
        open val notValidOtherTableValue: EmptyTable? = null
        fun getUnitValue() = Unit
    }

    open class NotATable(initMap: MutableMap<String, Any?>) {
        open val intValue: Int by initMap
    }

    open class EmptyTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap)

    open class IndirectTable(initMap: MutableMap<String, Any?> = default): BaseTable(initMap)

    open class DoubleIndirectTable(initMap: MutableMap<String, Any?> = default): BaseMiddleTable(initMap) {
        open val directValue: Int? by initMap
    }

    open class ReferenceTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        constructor(id: Long): this(init(id))

        open var normalTable: NormalTable? by initMap
        open var normalTableValue: NormalTable by initMap
        open var normalTableValues: List<NormalTable> by initMap
    }
}