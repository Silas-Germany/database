package com.github.silasgermany.complexormprocessor.models

import com.github.silasgermany.complexormapi.ComplexOrmTypes

data class ColumnType(
    val type: ComplexOrmTypes,
    val nullable: Boolean,
    val referenceTable: String?
)