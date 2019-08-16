package com.github.silasgermany.complexormprocessor.models

data class ColumnType(
    val type: InternComplexOrmTypes,
    val nullable: Boolean,
    val referenceTable: String?,
    val enumType: String? = null
)