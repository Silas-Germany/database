package com.github.silasgermany.complexormprocessor

import javax.lang.model.element.AnnotationMirror

data class Column(
    val name: String,
    val type: ColumnType,
    val annotations: List<AnnotationMirror>
)