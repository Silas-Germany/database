package com.github.silasgermany.complexormapi

abstract class ComplexOrmTable(val map: MutableMap<String, Any?>) {

    open val id: CommonUUID? by map

    override fun toString(): String {
        return map.toList().joinToString(prefix = "${this::class.simpleName!!}{", postfix = "}") { (key, value) ->
            "$key: " + when (value) {
                is ComplexOrmTable -> "ComplexOrmTable(${value.id ?: "?"})"
                is List<*> -> value.joinToString(prefix = "[", postfix = "]") {
                    "ComplexOrmTable(${(it as ComplexOrmTable).id?.toString() ?: "?"})"
                }
                is String -> "\'$value\'"
                is ByteArray -> "ByteArray(size: ${value.size})"
                null -> "null"
                else -> "$value"
            }
        }
    }

    @Suppress("unused")
    fun showRecursive(): String {
        return map.toList().joinToString(prefix = "${this::class.simpleName!!}{", postfix = "}") { (key, value) ->
            "$key: " + when (value) {
                is ComplexOrmTable -> value.showRecursive()
                is List<*> -> value.joinToString(prefix = "[", postfix = "]") {
                    (it as? ComplexOrmTable?)?.showRecursive() ?: "?"
                }
                is String -> "\'$value\'"
                is ByteArray -> "ByteArray(size: ${value.size})"
                null -> "null"
                else -> "$value"
            }
        }
    }

    override fun equals(other: Any?) = id?.let { it == (other as? ComplexOrmTable?)?.id } ?: false
    @Suppress("RedundantOverride")
    override fun hashCode() = super.hashCode()

    companion object {
        val default get() = mutableMapOf<String, Any?>("id" to null).run {
            withDefault {
                throw NoSuchElementException("Key does not exist: $it in $this")
            }
        }
        fun init(id: CommonUUID?) = default.also { it["id"] = id }
        @Suppress("unused")
        inline fun <reified T: ComplexOrmTable>T.cloneWithoutId(vararg values: Pair<String, Any?>): T {
            val newMap = default.apply {
                putAll(map)
                remove("id")
                putAll(values)
            }
            return null!!
            //return T::class.constructors.first().call(newMap)
        }
    }
}