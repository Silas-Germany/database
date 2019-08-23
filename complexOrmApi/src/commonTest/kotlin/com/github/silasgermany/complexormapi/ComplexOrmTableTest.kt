package com.github.silasgermany.complexormapi

import com.github.silasgermany.complexormapi.ComplexOrmTable.Companion.init
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ComplexOrmTableTest {

    class TestTable(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        class InnerTable(initMap: MutableMap<String, Any?> = default) : ComplexOrmTable(initMap) {
            var recursiveEntry: TestTable by initMap
        }

        var name by initMap
        var oneEntry: InnerTable by initMap
        var multipleEntries: List<InnerTable> by initMap
    }

    @Test fun testInit() {
        val table = TestTable()
        assertFailsWith(NoSuchElementException::class,
            "Attribute can't be read before written") { table.name }
        val expected = "TEST"
        table.name = expected
        assertEquals(expected, table.name, "Name is same as assigned")
    }

    @Test fun testPrint() {
        val table = TestTable()
        var expected = "TestTable"
        assertEquals(expected, table.shortClassName, "Class name is correct")
        table.name = "TEST"
        val id = generatedId
        val id2 = generatedId
        table.oneEntry = TestTable.InnerTable(init(id))
        table.multipleEntries = listOf(TestTable.InnerTable(init(id2)))
        expected = "TestTable{id: null, name: 'TEST', oneEntry: InnerTable($id), " +
                "multipleEntries: [InnerTable($id2)]}"
        assertEquals(expected, "$table", "Only ID reference is printed")
        expected = "TestTable{id: null, name: 'TEST', " +
                "oneEntry: InnerTable{id: $id}, " +
                "multipleEntries: [InnerTable{id: $id2}]}"
        assertEquals(expected, table.showRecursive(), "All data is printed")
        table.oneEntry.recursiveEntry = table
        expected = "TestTable{id: null, name: 'TEST', " +
                "oneEntry: InnerTable{" +
                "id: $id, " +
                "recursiveEntry: TestTable(?)" +
                "}, " +
                "multipleEntries: [InnerTable{id: $id2}]}"
        assertEquals(expected, table.showRecursive(), "All data is printed")
        table.multipleEntries.first().recursiveEntry = TestTable(init(id)).apply {
            oneEntry = table.multipleEntries.first()
        }
        expected = "TestTable{id: null, name: 'TEST', " +
                "oneEntry: InnerTable{" +
                "id: $id, " +
                "recursiveEntry: TestTable(?)" +
                "}, " +
                "multipleEntries: [InnerTable{" +
                "id: $id2, " +
                "recursiveEntry: TestTable{" +
                "id: $id, " +
                "oneEntry: InnerTable($id2)" +
                "}" +
                "}]}"
        assertEquals(expected, table.showRecursive(),
            "All is working, even if same ID with different table or different recursion depths of entries")
    }

    @Test fun testEquals() {
        val id = generatedId
        assertEquals(TestTable(init(id)), TestTable(init(IdType(id.bytes.copyOf()))))
        assertEquals(TestTable(init(id)).hashCode(), TestTable(init(IdType(id.bytes.copyOf()))).hashCode())
    }
}