package com.github.silasgermany.database.ui.main

import android.app.Activity
import android.os.Bundle
import java.io.File

class MainFragment: Activity(), MainView {

    override val databaseFolder: File get() = getExternalFilesDir("database")!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainPresenter(this)

    }
}