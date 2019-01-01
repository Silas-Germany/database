package com.github.silasgermany.database.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import org.jetbrains.anko.button
import org.jetbrains.anko.verticalLayout

class MainFragment: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firstTable = 1//GeneratedSqlAllTables.constructors.values.first().values.first()(mapOf("id" to 1, "name" to "test"))
        verticalLayout {
            gravity = Gravity.CENTER
            button("hello world: $firstTable ")
        }
    }
}