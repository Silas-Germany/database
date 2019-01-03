package com.github.silasgermany.complexorm

import android.database.Cursor

internal interface SqlUtils {

    fun <K, V> MutableMap<K, MutableList<V>>.add(key: K, value: V) = getOrPut(key) { mutableListOf() }.add(value)

    val CharSequence.sql: String
        get() = replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()

    val CharSequence.reverseUnderScore: String
        get() = replace("_[a-zA-Z]".toRegex()) { match -> match.value[1].toUpperCase().toString() }

    fun Cursor.forEach(f: (Cursor) -> Unit) {}
}