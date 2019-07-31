package com.github.silasgermany.complexormapi

import java.util.*
import java.util.UUID

actual typealias CommonUUID = UUID
actual object CommonUUIDObject {
    actual fun randomCommonUUID(): CommonUUID = UUID.randomUUID()

    actual fun nameUUIDFromBytes(name: String): CommonUUID = UUID.nameUUIDFromBytes(name.toByteArray())
}
