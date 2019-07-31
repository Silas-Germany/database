package com.github.silasgermany.complexormapi

data class Day(
    val year: Int,
    val month: Int,
    val day: Int
) {
    constructor(sqlDate: String) : this(sqlDate.split('-').map(String::toInt))

    private constructor(sqlDateParts: List<Int>) : this(sqlDateParts[0], sqlDateParts[1], sqlDateParts[2]) {
        if (sqlDateParts.size != 3) throw IllegalArgumentException("$sqlDateParts is not in the correct SQL format")
    }

    val asSql =
        "$year-${month.twoDigits}-${day.twoDigits}"

    private val Int.twoDigits get() =
        toString().padStart(2, '0')
}