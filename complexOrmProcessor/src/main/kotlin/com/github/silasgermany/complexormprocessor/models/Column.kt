package com.github.silasgermany.complexormprocessor.models

import javax.lang.model.element.AnnotationMirror
import kotlin.reflect.KClass

data class Column(
    val name: String,
    val columnType: ColumnType,
    val annotations: List<AnnotationMirror>
) {
    val columnName = name.replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()

    fun getAnnotationValue(annotationClass: KClass<out Annotation>): Any? {
        val hasAnnotation = annotations.find {
            "$it".removePrefix("@").startsWith(annotationClass.java.canonicalName)
        }?.elementValues?.values
        return hasAnnotation?.let { it.firstOrNull()?.value ?: Unit }
    }
}