package com.github.silasgermany.complexorm

@Suppress("NON_FINAL_MEMBER_IN_FINAL_CLASS")
expect class CommonDateTime(var1: Long) {

    open fun getYear(): Int
    fun getMonthOfYear(): Int
    fun getDayOfMonth(): Int
    fun getMillis(): Long

    fun plusMonths(var1: Int): CommonDateTime
    open fun toString(var1: String): String
}