package com.github.silasgermany.complexormapi

import kotlin.test.Test
import kotlin.test.assertEquals

class ExpectTest {

    // Not sure, how to test with minify and proguard rules...
    @Test fun className() {
        assertEquals("ExpectTest", ExpectTest::class.className, "Class name")
    }
}
