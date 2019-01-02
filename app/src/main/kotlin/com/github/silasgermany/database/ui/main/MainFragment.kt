package com.github.silasgermany.database.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import com.github.silasgermany.complexorm.Write
import org.jetbrains.anko.button
import org.jetbrains.anko.verticalLayout

class MainFragment: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firstTable = Write.write(MainModel.User())
        verticalLayout {
            gravity = Gravity.CENTER
            button("hello world: $firstTable ")
        }
    }
}