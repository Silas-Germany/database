package com.github.silasgermany.complexorm

import kotlin.test.Test
import kotlin.test.assertEquals

class ExpectTest {

	private val dateTime = CommonDateTime(1549202706000L)

	@Test fun getYearTest() {
		assertEquals(2019, dateTime.getYear())
	}

	@Test fun getMonthOfYearTest() {
		assertEquals(2, dateTime.getMonthOfYear())
	}

	@Test fun getDayOfMonthTest() {
		assertEquals(3, dateTime.getDayOfMonth())
	}

	@Test fun getMillisTest() {
		assertEquals(1549202706000L, dateTime.getMillis())
	}

	@Test fun toStringTest() {
		assertEquals("02,07,Sun", dateTime.toString("MM,hh,E"))
		assertEquals("Sunday 03 February 2019 19:35:06", dateTime.toString("EEEEE dd MMMMM yyyy HH:mm:ss"))
	}
}
