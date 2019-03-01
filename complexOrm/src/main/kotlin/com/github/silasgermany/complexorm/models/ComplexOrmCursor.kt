package com.github.silasgermany.complexorm.models

import android.content.ContentResolver
import android.database.*
import android.net.Uri
import android.os.Bundle

class ComplexOrmCursor(cursor: CrossProcessCursor, withColumnsInfo: Boolean = true): Cursor {

    private var window = CursorWindow(null)
    private var cursorPosition = 0
    private val count: Int
    private var closed = false
    private var columns: Array<String>? = if (withColumnsInfo) cursor.columnNames else null
    val valid: Boolean

    init {
        cursor.fillWindow(0, window)
        count = window.numRows
        valid = count == cursor.count
        if (valid) cursor.close()
    }

    override fun moveToFirst(): Boolean {
        cursorPosition = 0
        val result = count > 0
        return result
    }

    override fun moveToPosition(position: Int): Boolean {
        val result = takeIf { position in 0..(count - 1) }?.apply { cursorPosition = position } != null
        return result
    }

    override fun move(offset: Int): Boolean {
        val result = moveToPosition(cursorPosition + offset)
        return result
    }

    override fun moveToPrevious(): Boolean {
        val result = takeIf { cursorPosition > 0 }?.apply { cursorPosition-- } != null
        return result
    }

    override fun moveToNext(): Boolean {
        val result = takeIf { cursorPosition < count }?.apply { cursorPosition++ } != null
        return result
    }

    override fun moveToLast(): Boolean {
        val result = takeIf { count > 0 }?.apply { cursorPosition = count - 1 } != null
        return result
    }

    override fun isBeforeFirst(): Boolean {
        val result = cursorPosition < 0
        return result
    }

    override fun isFirst(): Boolean {
        val result = cursorPosition == 0
                return result
    }

    override fun isLast(): Boolean {
        val result = cursorPosition == count - 1
                return result
    }

    override fun isAfterLast(): Boolean {
        val result = cursorPosition >= count
                return result
    }

    override fun getPosition(): Int {
        val result = cursorPosition
                return result
    }

    override fun getCount(): Int {
        val result = count
                return result
    }

    override fun getType(columnIndex: Int): Int {
        val result = window.getType(cursorPosition, columnIndex)
                return result
    }

    override fun isNull(columnIndex: Int): Boolean {
        val result = window.getType(cursorPosition, columnIndex) == Cursor.FIELD_TYPE_NULL
                return result
    }

    override fun getShort(columnIndex: Int): Short {
        val result = window.getShort(cursorPosition, columnIndex)
                return result
    }

    override fun getInt(columnIndex: Int): Int {
        val result = window.getInt(cursorPosition, columnIndex)
                return result
    }

    override fun getLong(columnIndex: Int): Long {
        val result = window.getLong(cursorPosition, columnIndex)
                return result
    }

    override fun getFloat(columnIndex: Int): Float {
        val result = window.getFloat(cursorPosition, columnIndex)
                return result
    }

    override fun getDouble(columnIndex: Int): Double {
        val result = window.getDouble(cursorPosition, columnIndex)
                return result
    }

    override fun getString(columnIndex: Int): String {
        val result = window.getString(cursorPosition, columnIndex)
                return result
    }

    override fun getBlob(columnIndex: Int): ByteArray {
        val result = window.getBlob(cursorPosition, columnIndex)
                return result
    }

    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer?) {
        window.copyStringToBuffer(cursorPosition, columnIndex, buffer)
            }

    override fun getColumnCount(): Int {
        val result = columns?.size ?: throw IllegalAccessException("Cursor doesn't have column info activated")
                return result
    }

    override fun getColumnName(columnIndex: Int): String {
        val result = columns?.get(columnIndex) ?: throw IllegalAccessException("Cursor doesn't have column info activated")
                return result
    }

    override fun getColumnIndex(columnName: String?): Int {
        val result = columns?.indexOf(columnName) ?: throw IllegalAccessException("Cursor doesn't have column info activated")
                return result
    }

    override fun getColumnNames(): Array<String> {
        val result = columns ?: throw IllegalAccessException("Cursor doesn't have column info activated")
                return result
    }

    override fun getColumnIndexOrThrow(columnName: String?): Int {
        val result = columns?.indexOf(columnName).takeUnless { it == -1 }
            ?: throw IllegalAccessException("Couldn't find column $columnName in $columns")
                return result
    }

    override fun close() {
        window.close()
        closed = true
            }

    override fun isClosed(): Boolean {
        val result = closed
                return result
    }

    override fun registerContentObserver(observer: ContentObserver?) {
        throw UnsupportedOperationException()
    }

    override fun getNotificationUri(): Uri {
        throw UnsupportedOperationException()
    }

    override fun respond(extras: Bundle?): Bundle {
        throw UnsupportedOperationException()
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        throw UnsupportedOperationException()
    }

    override fun unregisterContentObserver(observer: ContentObserver?) {
        throw UnsupportedOperationException()
    }

    override fun getWantsAllOnMoveCalls(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        throw UnsupportedOperationException()
    }

    override fun setNotificationUri(cr: ContentResolver?, uri: Uri?) {
        throw UnsupportedOperationException()
    }

    override fun getExtras(): Bundle {
        throw UnsupportedOperationException()
    }

    override fun setExtras(extras: Bundle?) {
        throw UnsupportedOperationException()
    }

    @Deprecated("Since reQuery() is deprecated, so too is this.",
        ReplaceWith("throw IllegalAccessException(\"depreciated\")"))
    override fun deactivate() {
        throw IllegalAccessException("depreciated")
    }

    @Deprecated("Don't use this.",
        ReplaceWith("throw IllegalAccessException(\"depreciated\")"))
    override fun requery(): Boolean {
        throw IllegalAccessException("depreciated")
    }
}