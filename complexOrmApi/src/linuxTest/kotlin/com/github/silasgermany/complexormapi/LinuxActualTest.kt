package com.github.silasgermany.complexormapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class LinuxActualTest {

    @Test fun idClassCorrectlyImplemented() {
        val id = IdType(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16))
        assertEquals("x'0102030405060708090A0B0C0D0E0F10'", "$id")
        assertFails("Too few bytes") { IdType(ByteArray(15)) }
        assertFails("Too many bytes") { IdType(ByteArray(17)) }
        IdType(ByteArray(16))
    }
}
