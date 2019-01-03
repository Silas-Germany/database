package com.github.silasgermany.database.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import com.github.silasgermany.complexorm.Write
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class MainFragment: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = MainModel.User()
        user.name = "silas"
        user.lastName = "from Germany"
        val firstTable = Write.write(user)
        verticalLayout {
            gravity = Gravity.CENTER
            textView("hello world: $firstTable ") {
                textSize = 16f
            }
        }
    }
}