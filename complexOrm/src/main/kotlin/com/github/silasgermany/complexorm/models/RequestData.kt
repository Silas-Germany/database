package com.github.silasgermany.complexorm.models

import com.github.silasgermany.complexormapi.ComplexOrmTypes

class RequestData(
    tableName: String
){
    val columns = mutableListOf("'$tableName'.'id'")
    val tablesAndRestrictions = mutableListOf(tableName)
    var value: Any? = null
    lateinit var type: ComplexOrmTypes
}