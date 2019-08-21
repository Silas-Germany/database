package com.github.silasgermany.complexormapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class IdClass {

    @Test fun idClassCorrectlyImplemented() {
        val id = IdType(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))
        assertEquals("x'0102030405060708090a0b0c0d0e0f10'", id.asSql)
        assertEquals("01020304-0506-0708-090a-0b0c0d0e0f10", "$id")
        assertFails("Too few bytes") { IdType(ByteArray(15)) }
        assertFails("Too many bytes") { IdType(ByteArray(17)) }
        IdType(ByteArray(16))
        assertEquals(IdType(id.bytes).bytes.toList(), id.bytes.toList(),
            "Bytes return correct value")
    }

    @Test fun idClassEquals() {
        val id = generatedId
        assertEquals(id, IdType(id.bytes.copyOf()))
        assertEquals(id.hashCode(), IdType(id.bytes.copyOf()).hashCode())
    }
}
