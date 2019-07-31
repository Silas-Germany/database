package com.github.silasgermany.complexormapi

import java.util.*

actual typealias CommonUUID = UUID

actual fun randomCommonUUID(): CommonUUID = UUID.randomUUID()