package com.github.silasgermany.complexorm.models

class RequestInfo(
    private val tableName: String,
    private val tableClassName: String,
    private val where: String?,
    val readTableInfo: ReadTableInfo
){
    val columns = mutableListOf("'$tableName'.'id'")
    val tablesAndRestrictions = mutableListOf("'$tableName'")

    val query: String get() {
        readTableInfo.connectedColumn?.let { columns.add(it) }
        where?.let { tablesAndRestrictions.add(it) } ?: tablesAndRestrictions.add("WHERE 1")
        readTableInfo.restrictions[tableClassName]?.let { tablesAndRestrictions += "AND $it" }
        return "SELECT ${columns.joinToString()} FROM ${tablesAndRestrictions.joinToString(" ")};"
    }
}