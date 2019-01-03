package com.github.silasgermany.complexorm

import java.util.*
import kotlin.reflect.KClass

@SqlAllTables
object Write: SqlUtils {

    val sqlSchema =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlSchema").getDeclaredField("INSTANCE").get(null) as GeneratedSqlSchemaInterface
    val sqlTables =
        Class.forName("com.github.silasgermany.complexorm.GeneratedSqlTables").getDeclaredField("INSTANCE").get(null) as GeneratedSqlTablesInterface

    private fun getIdentifier(tableClass: KClass<*>) = tableClass.java.name
        .run { substring(lastIndexOf('.') + 1).split('$') }.let { it[0] to it[1].sql }

    private fun getNormalColumns(tableClass: KClass<*>) = getIdentifier(tableClass).run { sqlTables.normalColumns[first]!![second]!! }

    fun write(table: SqlTable): String {
        val normalColumns = getNormalColumns(table::class)
        val insertValues = mutableMapOf<String, String>()
        table.map.forEach { (key, value) ->
            val sqlKey = key.sql
            if (normalColumns[sqlKey] != null) {
                val formatedValue = when (value) {
                    SqlTypes.String -> "'$value'"
                    SqlTypes.Int -> value
                    SqlTypes.SqlTable,
                    SqlTypes.SqlTables -> throw IllegalArgumentException("Normal table shouldn't have SqlTable inside")
                    SqlTypes.Date -> (value as Date).time
                    else -> value
                }
                insertValues[key] = formatedValue.toString()
            }
        }
        return "INSERT INTO (${insertValues.keys.joinToString()}) VALUES (${insertValues.values.joinToString()});${normalColumns.keys};${table.map.keys}"
    }
}
