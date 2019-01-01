package com.github.silasgermany.database.ui.main

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.*

class MainFragment: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            gravity = Gravity.CENTER
            button("hello world")
        }
    }
}