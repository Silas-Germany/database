 gcc sqlite3.c -DSQLITE_HAS_CODEC -DSQLITE_TEMP_STORE=3 -DSQLCIPHER_CRYPTO_CC -DNDEBUG -framework Security -framework Foundation -c
