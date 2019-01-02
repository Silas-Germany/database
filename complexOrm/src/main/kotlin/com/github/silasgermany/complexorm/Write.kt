package com.github.silasgermany.complexorm

@SqlAllTables
object Write {
    fun write(table: SqlTable): String {
        val x = Class.forName("com.github.silasgermany.complexorm.GeneratedSqlSchema").getDeclaredField("INSTANCE").get(null) as GeneratedSqlSchemaInterface
        return x.tableNames.toString()
    }
}
