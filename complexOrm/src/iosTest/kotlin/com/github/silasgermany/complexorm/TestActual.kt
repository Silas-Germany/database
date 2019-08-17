package com.github.silasgermany.complexorm

actual val databaseSchema: com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
    get() = ComplexOrmDatabaseSchema()
actual val tableInfo: com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
    get() = ComplexOrmTableInfo()