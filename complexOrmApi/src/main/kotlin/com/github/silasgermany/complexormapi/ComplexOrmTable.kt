package com.github.silasgermany.complexormapi

abstract class ComplexOrmTable(val map: MutableMap<String, Any?>) {

    val id: Long? by map

    override fun toString(): String {
        return map.toList().joinToString(prefix = "${this::class.java.simpleName}{", postfix = "}") { (key, value) ->
            "$key: " + when (value) {
                is ComplexOrmTable -> "ComplexOrmTable(${value.id ?: "?"})"
                is List<*> -> value.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { "ComplexOrmTable(${(it as ComplexOrmTable).id?.toString() ?: "?"})" }
                is String -> "\'$value\'"
                is ByteArray -> "ByteArray(size: ${value.size})"
                null -> "null"
                else -> "$value"
            }
        }
    }

    companion object {
        val default get() = mutableMapOf<String, Any?>("id" to null).run {
            withDefault {
                throw NoSuchElementException("Key does not exist: $it in $this")
            }
        }
        fun init(id: Long?) = default.also { it["id"] = id }
        inline fun <reified T: ComplexOrmTable>create(id: Long) = T::class.java.getConstructor(Map::class.java).newInstance(init(id)) as T
    }
}