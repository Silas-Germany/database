package com.github.silasgermany.complexorm

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlTablesInterface

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlIgnore

abstract class SqlTable(@SqlIgnore val map: MutableMap<String, Any?>) {

    open var id: Int? by map

    companion object {
        val default get() = mutableMapOf<String, Any?>().run {
            withDefault {
                throw IllegalAccessException("Key does not exist: $it in $this")
            }
        }
    }
}