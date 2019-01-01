package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexorm.SqlAllTables
import com.github.silasgermany.complexorm.SqlIgnore
import com.github.silasgermany.complexorm.SqlIgnoreFunction
import com.github.silasgermany.complexorm.SqlTable
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types
import javax.tools.Diagnostic

class Main: AbstractProcessor(), SqlTypes {

    private lateinit var messager: Messager
    private lateinit var typeUtils: Types

    override fun init(p0: ProcessingEnvironment) {
        messager = p0.messager
        typeUtils = p0.typeUtils!!
        super.init(p0)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(SqlAllTables::class.java.canonicalName, SqlIgnore::class.java.canonicalName, SqlIgnoreFunction::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    private val rootTables = mutableListOf<Element>()
    private val internTables = mutableMapOf<Element, MutableList<Element>>()

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            val sqlTableName = SqlTable::class.java.canonicalName
            roundEnv.rootElements.forEach { rootElement ->
                rootElement.enclosedElements.forEach enclosedElement@ { enclosedElement ->
                    try {
                        if (enclosedElement.asType().toString().startsWith("(")) return@enclosedElement
                        typeUtils.directSupertypes(enclosedElement.asType()).forEach { superType ->
                            if (sqlTableName == superType.toString()) rootTables.add(enclosedElement)
                            typeUtils.directSupertypes(superType).forEach {
                                if (sqlTableName == it.toString()) internTables.add(rootElement, enclosedElement)
                            }
                        }
                    } catch (e: Exception) {
                        messager.printMessage(Diagnostic.Kind.NOTE, "Problem (${e.message}) with: $rootElement; $enclosedElement")
                    }
                }
            }
            messager.printMessage(Diagnostic.Kind.NOTE, "Result: $rootTables$internTables")
            return true
        } catch (e: Exception) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.message)
            e.stackTrace.forEach {
                messager.printMessage(Diagnostic.Kind.ERROR, "$it")
            }
            return false
        }
    }

    fun <K, V> MutableMap<K, MutableList<V>>.add(key: K, value: V) = getOrPut(key) { mutableListOf() }.add(value)
}
