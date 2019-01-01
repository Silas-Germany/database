package com.github.silasgermany.database.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import org.jetbrains.anko.button
import org.jetbrains.anko.verticalLayout
import org.sil.forchurches.rev79.model.database.GeneratedSqlAllTables

class MainFragment: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GeneratedSqlAllTables
        verticalLayout {
            gravity = Gravity.CENTER
            button("hello world")
        }
    }
}