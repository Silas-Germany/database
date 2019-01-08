package com.github.silasgermany.complexormapi

interface ComplexOrmSchemaInterface {
    val tableNames: List<String>
    val dropTableCommands: List<String>
    val createTableCommands: List<String>
}