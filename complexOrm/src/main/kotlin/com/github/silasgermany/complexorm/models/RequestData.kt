package com.github.silasgermany.complexorm.models

class RequestData(
    private val tableName: String,
    private val tableClassName: String,
    private val where: String?,
    val additionalRequestData: AdditionalRequestData
){
    val columns = mutableListOf("'$tableName'.'id'")
    val tablesAndRestrictions = mutableListOf("'$tableName'")

    val query: String get() {
        additionalRequestData.connectedColumn?.let { columns.add("'$tableName'.'$it'") }
        where?.let { tablesAndRestrictions.add(it) } ?: tablesAndRestrictions.add("WHERE 1")
        additionalRequestData.restrictions[tableClassName]?.let { tablesAndRestrictions += "AND $it" }
        return "SELECT ${columns.joinToString()} FROM ${tablesAndRestrictions.joinToString(" ")};"
    }
}