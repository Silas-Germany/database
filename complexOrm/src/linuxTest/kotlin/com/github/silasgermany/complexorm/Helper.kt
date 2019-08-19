package com.github.silasgermany.complexorm

actual open class Helper {
    init {
        _databaseSchema = ComplexOrmDatabaseSchema()
        _tableInfo = ComplexOrmTableInfo()
    }
}