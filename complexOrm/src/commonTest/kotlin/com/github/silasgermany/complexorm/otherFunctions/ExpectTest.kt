package com.github.silasgermany.complexorm.otherFunctions

import com.github.silasgermany.complexorm.helper.CommonHelper
import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.className
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpectTest: CommonHelper() {

	class InnerClass

	@Test fun testClassName() {
		assertEquals("Date", Date::class.className)
		assertEquals("InnerClass", InnerClass::class.className)
	}
}