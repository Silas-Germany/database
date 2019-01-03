package com.github.silasgermany.database.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import com.github.silasgermany.complexorm.SqlWriter
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class MainFragment: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = MainModel.User()
        val firstTable = SqlWriter.write(user)
        verticalLayout {
            gravity = Gravity.CENTER
            textView(firstTable) {
                textSize = 16f
            }
        }
    }
}