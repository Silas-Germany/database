package com.github.silasgermany.complexorm.models

import android.database.CrossProcessCursor
import android.database.Cursor
import android.database.CursorWindow
import com.github.silasgermany.complexorm.ComplexOrmCursor
import java.io.Closeable

class ComplexOrmCursor(cursor: Cursor): ComplexOrmCursor, Closeable {

    private val cursor: Cursor?
    private var window = CursorWindow(null)
    private var cursorPosition = 0
    private val count: Int

    init {
        if (cursor !is CrossProcessCursor) {
            this.cursor = cursor
            count = 0
        } else {
            cursor.fillWindow(0, window)
            count = window.numRows
            if (count == cursor.count) {
                this.cursor = null
                cursor.close()
            } else this.cursor = cursor
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
}