package com.github.silasgermany.complexormapi

@Suppress("MemberVisibilityCanBePrivate")
abstract class ComplexOrmTable(val map: MutableMap<String, Any?>) {

    open val id: IdType? by map

    val shortClassName get() = this::class.className

    @Suppress("unused")
    private val ComplexOrmTable.printWithId get() = "$shortClassName(${id?.nicePrint() ?: "?"})"

    override fun toString(): String {
        return map.toList().joinToString(prefix = "$shortClassName{", postfix = "}") { (key, value) ->
            "$key: " + when (value) {
                is ComplexOrmTable -> value.printWithId
                is List<*> -> value.joinToString(prefix = "[", postfix = "]") {
                    (it as ComplexOrmTable).printWithId
                }
                is String -> "\'$value\'"
                is ByteArray -> "ByteArray(size: ${value.size})"
                null -> "null"
                is IdType -> value.nicePrint()
                else -> "$value"
            }
        }
    }

    fun showRecursive(): String =
        showRecursive(mutableSetOf(shortClassName to id))
    fun showRecursive(alreadyPrinted: MutableSet<Pair<String, IdType?>>): String {
        return map.toList().joinToString(prefix = "$shortClassName{", postfix = "}") { (key, value) ->
            "$key: " + when (value) {
                is ComplexOrmTable -> {
                    if (alreadyPrinted.add(value.shortClassName to value.id)) value.showRecursive(alreadyPrinted)
                    else value.printWithId
                }
                is List<*> -> value.joinToString(prefix = "[", postfix = "]") {
                    it as ComplexOrmTable
                    if (alreadyPrinted.add(it.shortClassName to it.id)) it.showRecursive(alreadyPrinted)
                    else it.printWithId
                }
                is String -> "\'$value\'"
                is ByteArray -> "ByteArray(size: ${value.size})"
                null -> "null"
                is IdType -> value.nicePrint()
                else -> "$value"
            }
        }
    }

    override fun equals(other: Any?) = id?.let { it == (other as? ComplexOrmTable?)?.id } ?: false
    override fun hashCode() = id.hashCode()

    companion object {
        val default get() = mutableMapOf<String, Any?>("id" to null).run {
            withDefault {
                throw NoSuchElementException("Key does not exist: $it in $this")
            }
        }
        fun init(id: IdType?) = default.also { it["id"] = id }
    }
}