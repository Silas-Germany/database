package com.github.silasgermany.complexorm.models

import android.database.CrossProcessCursor
import com.github.silasgermany.complexorm.ComplexOrmCursor
import net.sqlcipher.Cursor
import net.sqlcipher.CursorWindow
import java.io.Closeable

class ComplexOrmCursor(cursor: Cursor): ComplexOrmCursor, Closeable {

    private val cursor: Cursor?
    private var window = CursorWindow(false)
    private var cursorPosition = 0
    val count: Int

    init {
        if (cursor !is CrossProcessCursor) {
            window.close()
            count = cursor.count
            this.cursor = cursor
        } else {
            cursor.fillWindow(0, window)
            count = window.numRows
            if (count == (cursor as Cursor).count) {
                this.cursor = null
            } else {
                window.close()
                this.cursor = cursor
            }
        }
    }

    override fun isNull(columnIndex: Int): Boolean {
        cursor?.apply { return isNull(columnIndex) }
        return window.getType(cursorPosition, columnIndex) == Cursor.FIELD_TYPE_NULL
    }

    override fun getInt(columnIndex: Int): Int {
        cursor?.apply { return getInt(columnIndex) }
        return window.getInt(cursorPosition, columnIndex)
    }

    override fun getLong(columnIndex: Int): Long {
        cursor?.apply { return getLong(columnIndex) }
        return window.getLong(cursorPosition, columnIndex)
    }

    override fun getFloat(columnIndex: Int): Float {
        cursor?.apply { return getFloat(columnIndex) }
        return window.getFloat(cursorPosition, columnIndex)
    }

    override fun getString(columnIndex: Int): String {
        cursor?.apply { return getString(columnIndex) }
        return window.getString(cursorPosition, columnIndex)
    }

    override fun getBlob(columnIndex: Int): ByteArray {
        cursor?.apply { return getBlob(columnIndex) }
        return window.getBlob(cursorPosition, columnIndex)
    }

    override fun close() {
        cursor?.apply { return close() }
        window.close()
    }

    fun moveToFirst(): Boolean {
        cursorPosition = 0
        return count > 0
    }

    fun moveToNext() =
        if (cursorPosition < count) {
            cursorPosition++
            true
        } else false
}