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
        return count > 0
    }

    override fun moveToPosition(position: Int): Boolean {
        return takeIf { position in 0 until count }?.apply { cursorPosition = position } != null
    }

    override fun move(offset: Int): Boolean {
        return moveToPosition(cursorPosition + offset)
    }

    override fun moveToPrevious(): Boolean {
        return takeIf { cursorPosition > 0 }?.apply { cursorPosition-- } != null
    }

    override fun moveToNext(): Boolean {
        return takeIf { cursorPosition < count }?.apply { cursorPosition++ } != null
    }

    override fun moveToLast(): Boolean {
        return takeIf { count > 0 }?.apply { cursorPosition = count - 1 } != null
    }

    override fun isBeforeFirst(): Boolean {
        return cursorPosition < 0
    }

    override fun isFirst(): Boolean {
        return cursorPosition == 0
    }

    override fun isLast(): Boolean {
        return cursorPosition == count - 1
    }

    override fun isAfterLast(): Boolean {
        return cursorPosition >= count
    }

    override fun getPosition(): Int {
        return cursorPosition
    }

    override fun getCount(): Int {
        return count
    }

    override fun getType(columnIndex: Int): Int {
        return window.getType(cursorPosition, columnIndex)
    }

    override fun isNull(columnIndex: Int): Boolean {
        return window.getType(cursorPosition, columnIndex) == Cursor.FIELD_TYPE_NULL
    }

    override fun getShort(columnIndex: Int): Short {
        return window.getShort(cursorPosition, columnIndex)
    }

    override fun getInt(columnIndex: Int): Int {
        return window.getInt(cursorPosition, columnIndex)
    }

    override fun getLong(columnIndex: Int): Long {
        return window.getLong(cursorPosition, columnIndex)
    }

    override fun getFloat(columnIndex: Int): Float {
        return window.getFloat(cursorPosition, columnIndex)
    }

    override fun getDouble(columnIndex: Int): Double {
        return window.getDouble(cursorPosition, columnIndex)
    }

    override fun getString(columnIndex: Int): String {
        return window.getString(cursorPosition, columnIndex)
    }

    override fun getBlob(columnIndex: Int): ByteArray {
        return window.getBlob(cursorPosition, columnIndex)
    }

    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer?) {
        window.copyStringToBuffer(cursorPosition, columnIndex, buffer)
            }

    override fun getColumnCount(): Int {
        return columns?.size ?: throw IllegalAccessException("Cursor doesn't have column info activated")
    }

    override fun getColumnName(columnIndex: Int): String {
        return columns?.get(columnIndex) ?: throw IllegalAccessException("Cursor doesn't have column info activated")
    }

    override fun getColumnIndex(columnName: String?): Int {
        return columns?.indexOf(columnName) ?: throw IllegalAccessException("Cursor doesn't have column info activated")
    }

    override fun getColumnNames(): Array<String> {
        return columns ?: throw IllegalAccessException("Cursor doesn't have column info activated")
    }

    override fun getColumnIndexOrThrow(columnName: String?): Int {
        return columns?.indexOf(columnName).takeUnless { it == -1 }
            ?: throw IllegalAccessException("Couldn't find column $columnName in $columns")
    }

    override fun close() {
        window.close()
        closed = true
            }

    override fun isClosed(): Boolean {
        return closed
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