package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmTypes

data class ColumnType(
    val type: ComplexOrmTypes,
    val nullable: Boolean,
    val referenceTable: String?
)