package com.github.silasgermany.complexormprocessor

import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class Processor(private val processingEnvironment: ProcessingEnvironment) {


    private val targetPackage = "com.github.silasgermany.complexorm"
    private val kaptKotlinGeneratedDir = processingEnvironment.options["kapt.kotlin.generated"]!!
    private val messager = processingEnvironment.messager

    private val tableExtractor = TableInfoExtractor(processingEnvironment.typeUtils)

    fun process(roundEnv: RoundEnvironment) {
        val tableInfo = tableExtractor.extract(roundEnv.rootElements)
        /*
        val schemaFileCreator = DatabaseSchemaFileCreator(tableInfo)
        var fileName = "ComplexOrmDatabaseSchema"
        var file = FileSpec.builder(targetPackage, fileName)
            .addType(
                TypeSpec.objectBuilder(fileName)
                    .addSuperinterface(ComplexOrmSchemaInterface::class)
                    .addProperty(schemaFileCreator.createNames())
                    .addProperty(schemaFileCreator.createDropTables())
                    .addProperty(schemaFileCreator.createCreateTables())
                    .build()
            ).build()
        file.writeTo(File(kaptKotlinGeneratedDir))

        val tableInfoFileCreator = TableInfoFileCreator(tableInfo)
        fileName = "ComplexOrmTableInfo"
        file = FileSpec.builder(targetPackage, fileName)
            .addType(
                TypeSpec.objectBuilder(fileName)
                    .addSuperinterface(ComplexOrmTablesInterface::class)
                    .addProperty(tableInfoFileCreator.createConstructors())
                    .addProperty(tableInfoFileCreator.createNormalColumnsInfo())
                    .addProperty(tableInfoFileCreator.createJoinColumnsInfo())
                    .addProperty(tableInfoFileCreator.createConnectedColumnsInfo())
                    .addProperty(tableInfoFileCreator.createReverseConnectedColumnsInfo())
                    .build()
            ).build()
        file.writeTo(File(kaptKotlinGeneratedDir))
        // */
        messager.printMessage(Diagnostic.Kind.ERROR, "$tableInfo")
    }
}