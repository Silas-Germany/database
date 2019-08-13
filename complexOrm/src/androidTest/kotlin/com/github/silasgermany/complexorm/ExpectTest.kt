package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.Day
import com.github.silasgermany.complexormapi.className
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ExpectTest {

    class InnerClass

    @Test fun className() {
        assertEquals("Day", Day::class.className)
        Class.forName("com.github.silasgermany.complexormapi.Day")
        assertEquals("InnerClass2", InnerClass::class.className)
        fail("all worked")
    }
}
