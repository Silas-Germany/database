package com.github.silasgermany.complexormapi

abstract class SqlTable(val map: MutableMap<String, Any?>) {

    val id: Long? get() = map["_id"] as Long?
    private val _id: Long? by map
    fun transferId() {
        map.remove("id")?.let { map["_id"] = it }
    }

    val idValue get() = (map.getOrElse("id") { null } ?: map.getOrElse("_id") { null }) as Long?

    override fun toString(): String {
        return map.toList().joinToString(prefix = "${this::class.java.simpleName}{", postfix = "}") { (key, value) ->
            "$key: " + when (value) {
                is SqlTable -> value.idValue ?: "?"
                is List<*> -> value.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { (it as? SqlTable)?.idValue?.toString() ?: "?" }
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