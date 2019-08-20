package com.github.silasgermany.complexorm

import com.github.silasgermany.complexorm.helper.CommonHelper
import kotlin.test.Test
import kotlin.test.assertEquals

class CommonDateTimeTest: CommonHelper() {

	private val dateTime = CommonDateTime(1549202706000L)

	@Test fun testGetYear() {
		assertEquals(2019, dateTime.getYear())
	}

	@Test fun testGetMonthOfYear() {
		assertEquals(2, dateTime.getMonthOfYear())
	}

	@Test fun testGetDayOfMonth() {
		assertEquals(3, dateTime.getDayOfMonth())
	}

	@Test fun testGetMillis() {
		assertEquals(1549202706000L, dateTime.getMillis())
	}

	@Test fun testToString() {
		assertEquals("02,07,Sun", dateTime.toString("MM,hh,E"))
		assertEquals("Sunday 03 February 2019 19:35:06", dateTime.toString("EEEEE dd MMMMM yyyy HH:mm:ss"))
	}
}
