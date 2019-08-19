package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface

actual val databaseSchema: ComplexOrmDatabaseSchemaInterface
    get() = _databaseSchema!!
var _databaseSchema: ComplexOrmDatabaseSchemaInterface? = null
actual val tableInfo: ComplexOrmTableInfoInterface
    get() = _tableInfo!!
var _tableInfo: ComplexOrmTableInfoInterface? = null
