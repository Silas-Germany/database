package com.github.silasgermany.complexorm

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlTablesInterface

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlAllTables


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlReverseJoinColumn(val connectedColumn: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlReverseConnectedColumn(val connectedColumn: String = "")


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlIgnore

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlIgnoreFunction

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SqlIgnoreClass

abstract class SqlTable(@SqlIgnore val map: MutableMap<String, Any?>) {

    open var id: Int? by map

    override fun toString(): String {
        return map.toList().joinToString(prefix = "${this::class.java.simpleName}{", postfix = "}") { (key, value) ->
            "$key: " + when (value) {
                is SqlTable -> value.map["id"] ?: "?"
                is List<*> -> value.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { (it as? SqlTable)?.id?.toString() ?: "?" }
                is String -> "\"$value\""
                null -> "nil"
                else -> "$value"
            }
        }
    }

    companion object {
        val default get() = mutableMapOf<String, Any?>().run {
            withDefault {
                throw IllegalAccessException("Key does not exist: $it in $this")
            }
        }
    }
}