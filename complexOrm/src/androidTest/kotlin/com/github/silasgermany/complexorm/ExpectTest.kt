package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.className
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpectTest {

    class InnerClass

    @Test fun className() {
        assertEquals("Date", Date::class.className)
        Class.forName("com.github.silasgermany.complexormapi.Date")
        assertEquals("InnerClass", InnerClass::class.className)
    }
}
