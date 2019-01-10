package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmAllTables
import com.github.silasgermany.complexormapi.ComplexOrmSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTable
import com.github.silasgermany.complexormapi.ComplexOrmTablesInterface
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Types
import javax.tools.Diagnostic

class Main: AbstractProcessor(), ComplexOrmUtils, ProcessAllTables, ProcessNormalTables {

    /* todo: Should work like this:
    Everything in the all-tables interface (marked with @ComplexOrmAllTables) is a database table, if it inherits somehow from ComplexOrmTable
    Everything inheriting from this is also a database table with the same name
    Everything else is not a database table, but a structure, that a database table can inherit from
    Columns of inherited tables will just be saved, if they have the column themselves through the override keyword
    All columns have to be marked with "by initMap" (otherwise they can't be saved or read)
    Every database table needs the constructor: "(initMap: MutableMap<String, Any?>)" (and can have "= default")
    No column is allowed to be read, that is not first written to or read by the database (see readable columns)
    todo: Needs column annotation: Read and save always; read always; save always (runtime default value)
    todo: Check again @ComplexOrmIgnore tag and "fun get..." properties: Should be completely ignored!!
    todo: Tables from the all-tables interface should also be normal tables (able to read and write them)
     */

    override lateinit var messager: Messager
    override lateinit var typeUtils: Types
    private lateinit var kaptKotlinGeneratedDir: String

    override fun init(p0: ProcessingEnvironment) {
        messager = p0.messager
        typeUtils = p0.typeUtils!!
        kaptKotlinGeneratedDir = p0.options["kapt.kotlin.generated"]!!
        super.init(p0)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            ComplexOrmAllTables::class.java.canonicalName
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override val rootTables = mutableListOf<Element>()
    override val rootAnnotations = mutableMapOf<String?, MutableList<Element>>()
    override val internAnnotations = mutableMapOf<Element, MutableList<Element>>()
    override val internTables = mutableMapOf<Element, MutableList<Element>>()

    private val targetPackage = "com.github.silasgermany.complexorm"

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            val sqlTableName = ComplexOrmTable::class.java.canonicalName
            roundEnv.rootElements.forEach { rootElement ->
                rootElement.enclosedElements.forEach enclosedElement@{ enclosedElement ->
                    try {
                        if (enclosedElement.asType().toString().startsWith("kotlin.jvm.functions.Function0") ||
                                enclosedElement.asType().kind == TypeKind.EXECUTABLE ) return@enclosedElement
                        typeUtils.directSupertypes(enclosedElement.asType()).forEach { superType ->
                            if (rootElement.getAnnotation(ComplexOrmAllTables::class.java) != null &&
                                sqlTableName == superType.toString()) rootTables.add(enclosedElement)
                            typeUtils.directSupertypes(superType).forEach {
                                if (sqlTableName == it.toString()) internTables.add(rootElement, enclosedElement)
                            }
                        }
                    } catch (e: Exception) {
                        messager.printMessage(
                            Diagnostic.Kind.NOTE,
                            "Problem (${e.message}) with: $rootElement; $enclosedElement"
                        )
                    }
                }
            }
            rootTables.forEach { rootTable ->
                rootTable.enclosedElements.forEach {
                    if ("$it".endsWith("\$annotations()")) rootAnnotations.add(rootTable.sql, it)
                }
            }
            internTables.forEach {
                it.value.forEach { table ->
                    table.enclosedElements.forEach { column ->
                        if ("$column".endsWith("\$annotations()")) internAnnotations.add(table, column)
                    }
                }
            }
            //messager.printMessage(Diagnostic.Kind.NOTE, "Result: $rootTables;$internTables;$rootAnnotations")
            var fileName = "ComplexOrmSchema"
            var file = FileSpec.builder(targetPackage, fileName)
                .addType(
                    TypeSpec.objectBuilder(fileName)
                        .addSuperinterface(ComplexOrmSchemaInterface::class)
                        .addProperty(createNames())
                        .addProperty(createTables())
                        .addProperty(createDropTables())
                        .addProperty(createCreateTables())
                        .build()
                ).build()
            file.writeTo(File(kaptKotlinGeneratedDir))
            fileName = "ComplexOrmTables"
            file = FileSpec.builder(targetPackage, fileName)
                .addType(
                    TypeSpec.objectBuilder(fileName)
                        .addSuperinterface(ComplexOrmTablesInterface::class)
                        .addProperty(createConstructors())
                        .addProperty(createNormalColumnsInfo())
                        .addProperty(createJoinColumnsInfo())
                        .addProperty(createConnectedColumnsInfo())
                        .addProperty(createReverseConnectedColumnsInfo())
                        .build()
                ).build()
            file.writeTo(File(kaptKotlinGeneratedDir))
            return true
        } catch (e: Exception) {
            //messager.printMessage(Diagnostic.Kind.ERROR, e.message)
            messager.printMessage(Diagnostic.Kind.ERROR, "${e.message};${e.stackTrace.joinToString()}")
            return false
        }
    }
}
