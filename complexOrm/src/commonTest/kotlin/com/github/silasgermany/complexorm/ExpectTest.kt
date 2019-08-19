package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.className
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpectTest: Helper() {

	class InnerClass

	@Test fun className() {
		assertEquals("Date", Date::class.className)
		assertEquals("InnerClass", InnerClass::class.className)
	}
}