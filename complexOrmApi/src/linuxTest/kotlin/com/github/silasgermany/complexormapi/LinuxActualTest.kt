package com.github.silasgermany.complexormapi

import platform.linux.RANDOM_UUID
import kotlin.test.*

class LinuxActualTest {

    @Test fun idClassCorrectlyImplemented() {
        assertEquals(ByteArray::class, IdType::class)
    }
}
