package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmDatabaseSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import org.joda.time.DateTimeZone
import org.joda.time.tz.UTCProvider

actual fun initCommonDateTime() {
    DateTimeZone.setProvider(UTCProvider())
}

actual val databaseSchema: ComplexOrmDatabaseSchemaInterface
    get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema")
        .newInstance() as ComplexOrmDatabaseSchemaInterface
actual val tableInfo: ComplexOrmTableInfoInterface
    get() = Class.forName("com.github.silasgermany.complexorm.ComplexOrmTableInfo")
        .newInstance() as ComplexOrmTableInfoInterface
