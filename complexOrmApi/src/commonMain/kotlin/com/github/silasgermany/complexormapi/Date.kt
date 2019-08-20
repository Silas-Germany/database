package com.github.silasgermany.complexormapi

data class Date(val year: Int, val month: Int, val day: Int) {
    constructor(sqlDate: String) : this(sqlDate.split('-').map(String::toInt))

    init {
        if (year !in 1000..4000) throw IllegalArgumentException("Year has to be between 1000 and 4000 (it is $year)")
        if (month !in 1..12) throw IllegalArgumentException("Month has to be between 1 and 12 (it is $month)")
        if (day !in 1..31) throw IllegalArgumentException("Day has to be between 1 and 31 (it is $day)")
    }

    private constructor(sqlDateParts: List<Int>) : this(sqlDateParts[0], sqlDateParts[1], sqlDateParts[2]) {
        if (sqlDateParts.size != 3) throw IllegalArgumentException("$sqlDateParts is not in the correct SQL format")
    }

    override fun toString() = "$year-${month.twoDigits}-${day.twoDigits}"
    val asSql by lazy { "'$this'" }

    private val Int.twoDigits get() =
        toString().padStart(2, '0')
}