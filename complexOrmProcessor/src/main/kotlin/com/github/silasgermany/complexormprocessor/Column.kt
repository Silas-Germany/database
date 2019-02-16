package com.github.silasgermany.complexormprocessor

import javax.lang.model.element.AnnotationMirror

data class Column(
    val name: String,
    val type: ColumnType,
    val annotations: List<AnnotationMirror>
) {
    val columnName = name.replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
}