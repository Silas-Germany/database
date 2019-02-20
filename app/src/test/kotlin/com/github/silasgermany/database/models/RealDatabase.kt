package com.github.silasgermany.database.models

import android.content.ContentValues
import android.database.CrossProcessCursor
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.github.silasgermany.complexorm.models.ComplexOrmCursor
import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface

class RealDatabase(private val database: SQLiteDatabase):
    ComplexOrmDatabaseInterface {
    override fun insertWithOnConflict(
        table: String,
        nullColumnHack: String,
        initialValues: ContentValues,
        conflictAlgorithm: Int
    ): Long {
        val result = database.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm)
        System.out.println("Insert $initialValues in $table (null -> $nullColumnHack, conflictAlgorithm: $conflictAlgorithm): $result")
        return result
    }

    override fun updateWithOnConflict(
        table: String,
        values: ContentValues,
        whereClause: String,
        whereArgs: Array<String>?,
        conflictAlgorithm: Int
    ): Int {
        val result = database.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm)
        System.out.println("Update $values in $table where $whereClause (whereArguments -> $whereArgs, conflictAlgorithm: $conflictAlgorithm): $result")
        return result
    }

    override fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int {
        val result = database.delete(table, whereClause, whereArgs)
        System.out.println("Delete from $table where $whereClause (whereArguments -> $whereArgs): $result")
        return result
    }

    override fun execSQL(sql: String) {
        System.out.println("Exec SQL: $sql")
        database.execSQL(sql)
    }

    override fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor {
        val result = database.rawQuery(sql, selectionArgs)
        System.out.println("Query Sql: $sql")// (whereArguments -> $selectionArgs): $result")
        return result
    }

    override fun queryForEach(sql: String, f: (Cursor) -> Unit) {
        rawQuery(sql, null)
            .let { cursor ->
                (cursor as? CrossProcessCursor)
                    ?.let { ComplexOrmCursor(it) }
                    ?.takeIf { it.valid }
                    ?: cursor
            }
            .use {
                it.moveToFirst()
                repeat(it.count) { _ ->
                    f(it)
                    it.moveToNext()
                }
            }
    }
}