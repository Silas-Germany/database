package com.github.silasgermany.database.models

import android.content.ContentResolver
import android.database.CharArrayBuffer
import android.database.ContentObserver
import android.database.Cursor
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle

class TestCursor: Cursor {
    override fun setNotificationUri(cr: ContentResolver?, uri: Uri?) {
        System.out.println("Called: setNotificationUri")
    }

    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer?) {
        System.out.println("Called: copyStringToBuffer")
    }

    override fun getExtras(): Bundle {
        System.out.println("Called: getExtras")
        return Bundle.EMPTY
    }

    override fun setExtras(extras: Bundle?) {
        System.out.println("Called: setExtras")
    }

    override fun moveToPosition(position: Int): Boolean {
        System.out.println("Called: moveToPosition")
        return true
    }

    private var longValue = 1L
    override fun getLong(columnIndex: Int): Long {
        System.out.println("Called: getLong")
        return longValue++
    }

    override fun moveToFirst(): Boolean {
        System.out.println("Called: moveToFirst")
        return true
    }

    override fun getFloat(columnIndex: Int): Float {
        System.out.println("Called: getFloat")
        return 1F
    }

    override fun moveToPrevious(): Boolean {
        System.out.println("Called: moveToPrevious")
        return true
    }

    override fun getDouble(columnIndex: Int): Double {
        System.out.println("Called: getDouble")
        return Double.MAX_VALUE
    }

    override fun close() {
        System.out.println("Called: close")
    }

    override fun isClosed(): Boolean {
        System.out.println("Called: isClosed")
        return true
    }

    override fun getCount(): Int {
        System.out.println("Called: result")
        return 2
    }

    override fun isFirst(): Boolean {
        System.out.println("Called: isFirst")
        return true
    }

    override fun isNull(columnIndex: Int): Boolean {
        System.out.println("Called: isNull")
        return false
    }

    override fun unregisterContentObserver(observer: ContentObserver?) {
        System.out.println("Called: unregisterContentObserver")
    }

    override fun getColumnIndexOrThrow(columnName: String?): Int {
        System.out.println("Called: getColumnIndexOrThrow")
        return 1
    }

    override fun requery(): Boolean {
        System.out.println("Called: requery")
        return true
    }

    override fun getWantsAllOnMoveCalls(): Boolean {
        System.out.println("Called: getWantsAllOnMoveCalls")
        return true
    }

    override fun getColumnNames(): Array<String> {
        System.out.println("Called: getColumnNames")
        return arrayOf()
    }

    override fun getInt(columnIndex: Int): Int {
        System.out.println("Called: getInt")
        return 1
    }

    override fun isLast(): Boolean {
        System.out.println("Called: isLast")
        return true
    }

    override fun getType(columnIndex: Int): Int {
        System.out.println("Called: getType")
        return 1
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        System.out.println("Called: registerDataSetObserver")
    }

    override fun moveToNext(): Boolean {
        System.out.println("Called: moveToNext")
        return true
    }

    override fun getPosition(): Int {
        System.out.println("Called: getPosition")
        return 1
    }

    override fun isBeforeFirst(): Boolean {
        System.out.println("Called: isBeforeFirst")
        return true
    }

    override fun registerContentObserver(observer: ContentObserver?) {
        System.out.println("Called: registerContentObserver")
    }

    override fun moveToLast(): Boolean {
        System.out.println("Called: moveToLast")
        return true
    }

    override fun deactivate() {
        System.out.println("Called: deactivate")
    }

    override fun getNotificationUri(): Uri {
        System.out.println("Called: getNotificationUri")
        return Uri.EMPTY
    }

    override fun getColumnName(columnIndex: Int): String {
        System.out.println("Called: getColumnName")
        return ""
    }

    override fun getColumnIndex(columnName: String?): Int {
        System.out.println("Called: getColumnIndex")
        return 1
    }

    override fun getBlob(columnIndex: Int): ByteArray {
        System.out.println("Called: getBlob")
        return ByteArray(0)
    }

    override fun getShort(columnIndex: Int): Short {
        System.out.println("Called: getShort")
        return 1
    }

    override fun getString(columnIndex: Int): String {
        System.out.println("Called: getString")
        return ""
    }

    override fun move(offset: Int): Boolean {
        System.out.println("Called: move")
        return true
    }

    override fun getColumnCount(): Int {
        System.out.println("Called: getColumnCount")
        return 1
    }

    override fun respond(extras: Bundle?): Bundle {
        System.out.println("Called: respond")
        return Bundle.EMPTY
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        System.out.println("Called: unregisterDataSetObserver")
    }

    override fun isAfterLast(): Boolean {
        System.out.println("Called: isAfterLast")
        return true
    }
}