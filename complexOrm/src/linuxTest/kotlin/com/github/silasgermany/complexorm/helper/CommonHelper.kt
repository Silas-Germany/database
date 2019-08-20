package com.github.silasgermany.complexorm.helper

import com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema
import com.github.silasgermany.complexorm.ComplexOrmTableInfo
import com.github.silasgermany.complexorm._databaseSchema
import com.github.silasgermany.complexorm._tableInfo

actual open class CommonHelper {
    init {
        _databaseSchema = ComplexOrmDatabaseSchema()
        _tableInfo = ComplexOrmTableInfo()
    }
}