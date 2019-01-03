package com.github.silasgermany.complexormapi

interface GeneratedSqlSchemaInterface {
    val tableNames: List<String>
    val dropTableCommands: List<String>
    val createTableCommands: List<String>
}