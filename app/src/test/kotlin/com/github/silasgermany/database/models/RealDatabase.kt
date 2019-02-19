package com.github.silasgermany.database.models

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.github.silasgermany.complexorm.ComplexOrmDatabaseInterface

class RealDatabase(private val database: SQLiteDatabase): ComplexOrmDatabaseInterface {
    override fun insertWithOnConflict(
        table: String,
        nullColumnHack: String,
        initialValues: ContentValues,
        conflictAlgorithm: Int
    ): Long {
        System.out.println("Insert $initialValues in $table (null -> $nullColumnHack, conflictAlgorithm: $conflictAlgorithm)")
        return database.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm)
    }

    override fun updateWithOnConflict(
        table: String,
        values: ContentValues,
        whereClause: String,
        whereArgs: Array<String>?,
        conflictAlgorithm: Int
    ): Int {
        System.out.println("Update $values in $table where $whereClause (whereArguments -> $whereArgs, conflictAlgorithm: $conflictAlgorithm)")
        return database.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm)
    }

    override fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int {
        System.out.println("Delete from $table where $whereClause (whereArguments -> $whereArgs)")
        return database.delete(table, whereClause, whereArgs)
    }

    override fun execSQL(sql: String) {
        System.out.println("Exec SQL: $sql")
        database.execSQL(sql)
    }

    override fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor {
        System.out.println("Query Sql: $sql (whereArguments -> $selectionArgs)")
        return database.rawQuery(sql, selectionArgs)
    }

    override fun queryForEach(sql: String, f: (Cursor) -> Unit) {
        rawQuery(sql, null).use {
            it.moveToFirst()
            repeat(it.count) { _ ->
                f(it)
                it.moveToNext()
            }
        }
    }
}