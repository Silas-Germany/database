package com.github.silasgermany.complexormapi

import kotlin.test.*

class ExpectTest {

    // Not sure, how to test with minify and proguard rules...
    @Test fun className() {
        assertEquals("ExpectTest", ExpectTest::class.className, "Class name")
    }
}
