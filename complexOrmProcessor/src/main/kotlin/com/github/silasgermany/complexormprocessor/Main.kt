package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmAllTables
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class Main: AbstractProcessor() {

    override fun getSupportedAnnotationTypes() = mutableSetOf(ComplexOrmAllTables::class.qualifiedName)
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()
    private val processor by lazy { Processor(processingEnv) }
    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        return try {
            processor.process(roundEnv)
            true
        } catch (e: Exception) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "${e.message} at: ${e.stackTrace.joinToString()}")
            false
        }
    }
}
