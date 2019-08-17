package com.github.silasgermany.complexorm

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonDateTimeTest {

	private val dateTime = CommonDateTime(1549202706000L)

	@Test fun getYear() {
		assertEquals(2019, dateTime.getYear())
	}

	@Test fun getMonthOfYear() {
		assertEquals(2, dateTime.getMonthOfYear())
	}

	@Test fun getDayOfMonth() {
		assertEquals(3, dateTime.getDayOfMonth())
	}

	@Test fun getMillis() {
		assertEquals(1549202706000L, dateTime.getMillis())
	}

	@Test fun toStringTest() {
		assertEquals("02,07,Sun", dateTime.toString("MM,hh,E"))
		assertEquals("Sunday 03 February 2019 19:35:06", dateTime.toString("EEEEE dd MMMMM yyyy HH:mm:ss"))
	}
}
