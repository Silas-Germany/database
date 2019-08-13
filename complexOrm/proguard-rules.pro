# Fix for error connected with sqlcipher (in case this library is used)
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.* { *; }

# Keep generated file
-keep class com.github.silasgermany.complexorm.ComplexOrmDatabaseSchema { *; }
-keep class com.github.silasgermany.complexorm.ComplexOrmTableInfo { *; }

# Keep all database-model files
-keepnames class * extends com.github.silasgermany.complexormapi.ComplexOrmTable { *; }
