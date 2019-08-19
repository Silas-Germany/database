package com.github.silasgermany.complexormapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.fail

class DateTest {

    @Test fun twoDigits() {
        val day = Date(2000, 1, 1)
        assertEquals("'2000-01-01'", day.asSql, "Has two digits for month and day")
    }

    @Test fun invalidDates() {
        try {
            assertFails("Year to small") { Date(999, 1, 1) }
            Date(1000, 1, 1)
            assertFails("Year to big") { Date(4001, 1, 1) }
            Date(4000, 1, 1)
            assertFails("Month to small") { Date(2000, 0, 1) }
            assertFails("Month to big") { Date(2000, 13, 1) }
            Date(2000, 12, 1)
            assertFails("Day to small") { Date(2000, 1, 0) }
            assertFails("Day to big") { Date(2000, 1, 32) }
            Date(2000, 1, 31)
        } catch (e: Throwable) {
            fail(e.message)
        }
    }

    @Test fun sqlConversion() {
        var day = Date(2000, 1, 1)
        assertEquals(Date("2000-01-01"), day, "Parses correctly ($day)")
        assertEquals("'2000-01-01'", day.asSql, "Parses correctly ($day)")
        day = Date(2000, 12, 1)
        assertEquals(Date("2000-12-01"), day, "Parses correctly ($day)")
        assertEquals("'2000-12-01'", day.asSql, "Parses correctly ($day)")
        day = Date(2000, 1, 31)
        assertEquals(Date("2000-01-31"), day, "Parses correctly ($day)")
        assertEquals("'2000-01-31'", day.asSql, "Parses correctly ($day)")
    }
}
