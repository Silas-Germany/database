package com.github.silasgermany.complexormapi

import java.util.*
import kotlin.test.*

class AndroidActualTest {

    @Test fun idClassCorrectlyImplemented() {
        val id = IdType(UUID.randomUUID()).let { it }
        assertEquals(IdType(id.bytes).bytes.toList(), id.bytes.toList(),
            "Bytes return correct value")
    }
}
