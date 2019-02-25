package com.github.silasgermany.database.models

import android.content.ContentValues
import android.database.Cursor
import com.github.silasgermany.complexorm.models.ComplexOrmDatabaseInterface

class TestDatabase: ComplexOrmDatabaseInterface {
    override fun beginTransaction() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setTransactionSuccessful() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun endTransaction() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insertWithOnConflict(
        table: String,
        nullColumnHack: String,
        initialValues: ContentValues,
        conflictAlgorithm: Int
    ): Long {
        System.out.println("Insert $initialValues in $table (null -> $nullColumnHack, conflictAlgorithm: $conflictAlgorithm)")
        return 0L
    }

    override fun updateWithOnConflict(
        table: String,
        values: ContentValues,
        whereClause: String,
        whereArgs: Array<String>?,
        conflictAlgorithm: Int
    ): Int {
        System.out.println("Update $values in $table where $whereClause (whereArguments -> $whereArgs, conflictAlgorithm: $conflictAlgorithm)")
        return 1
    }

    override fun delete(table: String, whereClause: String, whereArgs: Array<String>?): Int {
        System.out.println("Delete from $table where $whereClause (whereArguments -> $whereArgs)")
        return 1
    }

    override fun execSQL(sql: String) {
        System.out.println("Exec SQL: $sql")
    }

    override fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor? {
        System.out.println("Query Sql: $sql (whereArguments -> $selectionArgs)")
        return null
    }

    private val cursor = TestCursor()
    override fun queryForEach(sql: String, f: (Cursor) -> Unit) {
        System.out.println("Query Sql: $sql")
        f(cursor)
        f(cursor)
    }
}