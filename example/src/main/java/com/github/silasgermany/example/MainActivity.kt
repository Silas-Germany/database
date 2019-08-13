package com.github.silasgermany.example

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.github.silasgermany.complexorm.ComplexOrmWriter
import com.github.silasgermany.complexormapi.ComplexOrmAllTables
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.className

@ComplexOrmAllTables
class TestClass(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
    val name: String by map
    class InnerTestClass(initMap: MutableMap<String, Any?> = default): ComplexOrmTable(initMap) {
        val name: String by map
    }
}

class MainActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("XX", TestClass::class.className)
        Log.e("XX", TestClass.InnerTestClass::class.className)
        Log.e("XX", ComplexOrmTable::class.className)
        Log.e("XX", ComplexOrmTable::class.className)
        Log.e("XX", ComplexOrmWriter::class.className)
        Log.e("XX", TestClass().name)
        Log.e("XX", TestClass.InnerTestClass().name)
        Log.e("XX", TestClass.InnerTestClass().name)

        super.onCreate(savedInstanceState)
    }
}