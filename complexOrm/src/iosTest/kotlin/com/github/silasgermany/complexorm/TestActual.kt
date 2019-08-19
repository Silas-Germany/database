package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface

actual val databaseSchema: ComplexOrmDatabaseSchemaInterface
    get() = ComplexOrmDatabaseSchema()
actual val tableInfo: ComplexOrmTableInfoInterface
    get() = ComplexOrmTableInfo()