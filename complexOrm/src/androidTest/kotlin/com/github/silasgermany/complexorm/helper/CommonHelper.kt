package com.github.silasgermany.complexorm.helper

import org.joda.time.DateTimeZone

actual open class CommonHelper {
    init {
        DateTimeZone.setDefault(DateTimeZone.forOffsetHoursMinutes(5, 30))
    }
}