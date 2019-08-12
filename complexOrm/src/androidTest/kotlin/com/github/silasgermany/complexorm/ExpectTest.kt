package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.Day
import com.github.silasgermany.complexormapi.className
import com.github.silasgermany.complexormapi.className2
import kotlin.test.*

class ExpectTest {

    class InnerClass

    @Test fun className() {
        assertEquals(Day::class.className, "Day")
        assertEquals(Day::class.className2, "com.github.silasgermany.complexormapi.Day")
        Class.forName("com.github.silasgermany.complexormapi.Day")
    }
}
