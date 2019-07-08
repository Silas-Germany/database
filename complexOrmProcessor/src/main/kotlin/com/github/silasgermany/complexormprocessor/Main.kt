package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmAllTables
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class Main: AbstractProcessor() {

    override fun getSupportedAnnotationTypes() = mutableSetOf(ComplexOrmAllTables::class.java.canonicalName)
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()
    private val processor by lazy { Processor(processingEnv) }
    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        return try {
            val allTablesInterface = roundEnv.getElementsAnnotatedWith(ComplexOrmAllTables::class.java)
            if (allTablesInterface.size > 1) {
                throw IllegalArgumentException("The project can't have more than one interface marked with @ComplexOrmAllTables (it has: ${allTablesInterface.joinToString()})")
            }
            processor.process(roundEnv)
            true
        } catch (e: Error) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "${e.stackTrace.map { "${it.fileName};${it.lineNumber}" }}")
            //processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "${e} at: ${e.stackTrace.joinToString()}")
            //processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "${e.message} at: ${e.stackTrace.joinToString()}")
            false
        }
    }
}
