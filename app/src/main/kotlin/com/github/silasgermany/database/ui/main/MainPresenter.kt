package com.github.silasgermany.database.ui.main

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.github.silasgermany.complexorm.ComplexOrm
import com.github.silasgermany.database.ui.main.MainModel.Table
import com.github.silasgermany.database.ui.main.MainModel.Table1
import java.io.File

class MainPresenter(view: MainView) {

    private val databaseFile = File(view.databaseFolder, "database.db")
        //.apply { delete() }
    private val database = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)

    init {
        try {
            val database = ComplexOrm(database)
            database.createAllTables()
            val entry = Table1()
            entry.value1 = "test"
            entry.value3 = 1
            entry.value5 = null
            //entry.reverseEntries1 = listOf(Table4x().apply { entry1 = entry })
            //entry.reverseEntries2 = listOf(Table4x().apply { entries1 = listOf(Table2()) })
            //entry.entry1 = Table2()
            //entry.entries1 = listOf(Table2())
            Log.e("DATABASE_LOG", "$entry")
            database.complexOrmWriter.save(entry)
            Log.e("DATABASE_LOG", "$entry")
            val readEntry = database.query.get<Table>()
            Log.e("DATABASE_LOG", "$readEntry")
        } catch (e: Exception) {
            Log.e("DATABASE_LOG", "$e;${e.message}; ${e.stackTrace?.contentDeepToString()}", e)
        }
    }
    // */
}