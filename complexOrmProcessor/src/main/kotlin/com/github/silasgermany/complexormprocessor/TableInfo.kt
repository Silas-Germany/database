package com.github.silasgermany.complexormprocessor

data class TableInfo(
    val columns: MutableList<Column>,
    val superTable: String?
)
