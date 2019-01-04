package com.github.silasgermany.database.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import java.io.File

class MainFragment: Activity(), MainView {

    override val databaseFolder: File get() = getExternalFilesDir("database")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainPresenter(this)

        verticalLayout {
            gravity = Gravity.CENTER
            textView("working") {
                textSize = 16f
            }
        }
    }
}