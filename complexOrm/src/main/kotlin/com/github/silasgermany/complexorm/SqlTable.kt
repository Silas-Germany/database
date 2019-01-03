package com.github.silasgermany.complexorm

abstract class SqlTable(val map: MutableMap<String, Any?>) {

    open val id: Int? get() = map["_id"] as Int?
    private val _id: Int? by map
    fun transferId() {
        map.remove("id")?.let { map["_id"] = it }
    }

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