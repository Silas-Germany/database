package com.github.silasgermany.example

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.github.silasgermany.complexorm.ComplexOrmWriter
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.className
import com.github.silasgermany.complexormapi.className2

class TestClass
class MainActivity: Activity() {
    private class InnerTestClass
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("XX", TestClass::class.className)
        Log.e("XX", InnerTestClass::class.className)
        Log.e("XX", InnerTestClass::class.className2)
        Log.e("XX", ComplexOrmTable::class.className)
        Log.e("XX", ComplexOrmTable::class.className)
        Log.e("XX", ComplexOrmWriter::class.className)
        super.onCreate(savedInstanceState)
    }
}