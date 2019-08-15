package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.Date
import com.github.silasgermany.complexormapi.className
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpectTest {

	class InnerClass

	@Test fun className() {
		assertEquals("Date", Date::class.className)
		assertEquals("InnerClass", InnerClass::class.className)
	}

	@Test fun date() {
		initCommonDateTime()
		val dateTime = CommonDateTime(1565885791000L)
		assertEquals(2019, dateTime.getYear())
		assertEquals(8, dateTime.getMonthOfYear())
		assertEquals(15, dateTime.getDayOfMonth())
		assertEquals(1565885791000L, dateTime.getMillis())
		assertEquals("2019-08-15", dateTime.toString("yyyy-MM-dd"))
	}
}
