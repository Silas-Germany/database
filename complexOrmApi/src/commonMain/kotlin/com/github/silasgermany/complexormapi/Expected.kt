package com.github.silasgermany.complexormapi

@Suppress("UNUSED")
expect class CommonUUID {
    fun getLeastSignificantBits(): Long
    fun getMostSignificantBits(): Long
}
expect fun randomCommonUUID(): CommonUUID
