package com.github.silasgermany.database.ui.main

import android.util.Log
import com.github.silasgermany.complexorm.oldVersion.ComplexOrmDatabase
import com.github.silasgermany.database.ui.main.MainModel.*
import java.io.File

class MainPresenter(val view: MainView) {

    private val databaseFile = File(view.databaseFolder, "database.db")

    init {
        try {
            val database = ComplexOrmDatabase(databaseFile)
            val entry = Table1()
            entry.value1 = "test"
            entry.value3 = 1
            entry.value5 = null
            entry.reverseEntries1 = listOf(Table4x().apply { entry1 = entry })
            entry.reverseEntries2 = listOf(Table4x().apply { entries1 = listOf(Table2()) })
            entry.entry1 = Table2()
            entry.entries1 = listOf(Table2())
            //database.writer.write(entry)
            //val readEntry = database.reader.get<Table>()
            //Log.e("DATABASE", "$readEntry")
        } catch (e: Exception) {
            Log.e("DATABASE", "${e.message}; ${e.stackTrace.joinToString()}")
        }
    }
}