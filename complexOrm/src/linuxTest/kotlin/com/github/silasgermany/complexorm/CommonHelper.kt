package com.github.silasgermany.complexorm

actual open class CommonHelper: Helper() {
    init {
        _databaseSchema = ComplexOrmDatabaseSchema()
        _tableInfo = ComplexOrmTableInfo()
    }
}