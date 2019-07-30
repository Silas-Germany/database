package com.github.silasgermany.complexormapi

@Suppress("UNUSED")
expect class UUID {
    fun getLeastSignificantBits(): Long
    fun getMostSignificantBits(): Long
}
