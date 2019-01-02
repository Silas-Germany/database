package com.github.silasgermany.complexorm

interface GeneratedSqlSchemaInterface {
    val tableNames: List<String>
    val dropTableCommands: List<String>
    val createTableCommands: List<String>
}