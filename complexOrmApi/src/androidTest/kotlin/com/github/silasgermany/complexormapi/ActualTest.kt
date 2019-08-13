package com.github.silasgermany.complexormapi

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

actual class ActualTest {

    @Test fun idClassCorrectlyImplemented() {
        val id = IdType(UUID.randomUUID()).let { it }
        assertEquals(IdType(id.bytes).bytes.toList(), id.bytes.toList(),
            "Bytes return correct value")
    }
}
