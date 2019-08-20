package com.github.silasgermany.complexorm.helper

import kotlin.test.fail

inline fun assertFailsNot(message: String? = null, block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        fail("$message; ${e.message}")
    }
}
