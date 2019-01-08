package com.github.silasgermany.complexormapi

abstract class SqlTable(val map: MutableMap<String, Any?>) {

    val id: Long? get() = map["_id"] as Long?
    private val _id: Long? by map

    override fun toString(): String {
        return map.toList().joinToString(prefix = "${this::class.java.simpleName}{", postfix = "}") { (key, value) ->
            "$key: " + when (value) {
                is SqlTable -> value.id ?: "?"
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