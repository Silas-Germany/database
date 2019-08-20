package com.github.silasgermany.complexorm.helper

import org.joda.time.DateTimeZone
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner::class)
actual open class CommonHelper {
    init {
        DateTimeZone.setDefault(DateTimeZone.forOffsetHoursMinutes(5, 30))
    }
    @Test fun empty() {}
}