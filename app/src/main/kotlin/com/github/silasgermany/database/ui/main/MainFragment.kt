package com.github.silasgermany.database.ui.main

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import com.github.silasgermany.complexorm.SqlWriter.write
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import java.io.File

class MainFragment: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val user = MainModel.User()
            user.name = "ME"
            user.championedLanguages = listOf(MainModel.Language().apply { name = "?" })
            user.spokenLanguages = listOf(MainModel.Language().apply { name = "??" })
            user.interfaceLanguage = MainModel.Language().apply { name = "??" }
            val firstTable = write(user, File(getExternalFilesDir("database"), "database.db").path)
            verticalLayout {
                gravity = Gravity.CENTER
                textView(firstTable) {
                    textSize = 16f
                }
            }
        } catch (e: Exception) {
            Log.e("DATABASE", "${e.message}")
        }
    }
}