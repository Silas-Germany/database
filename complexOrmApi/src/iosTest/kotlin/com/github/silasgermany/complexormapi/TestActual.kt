package com.github.silasgermany.complexormapi

import kotlin.random.Random

actual val generatedId: IdType get() = IdType(Random.nextBytes(16))