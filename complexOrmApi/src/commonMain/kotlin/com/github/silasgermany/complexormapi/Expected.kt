package com.github.silasgermany.complexormapi

@Suppress("UNUSED")
expect class CommonUUID(mostSigBits: Long, leastSigBits: Long) {
    fun getLeastSignificantBits(): Long
    fun getMostSignificantBits(): Long
}
expect object CommonUUIDObject {
    fun randomCommonUUID(): CommonUUID
    fun nameUUIDFromBytes(name: String): CommonUUID
}