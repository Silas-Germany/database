package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTablesInterface
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class Processor(private val processingEnvironment: ProcessingEnvironment) {


    private val targetPackage = "com.github.silasgermany.complexorm"
    private val kaptKotlinGeneratedDir = processingEnvironment.options["kapt.kotlin.generated"]!!

    private val tableExtractor = TableInfoExtractor(processingEnvironment.typeUtils)

    fun process(roundEnv: RoundEnvironment) {
        val tableInfo = tableExtractor.extract(roundEnv.rootElements)
        val schemaFileCreator = FileCreatorDatabaseSchema(tableInfo)
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

        val tableInfoFileCreator = FileCreatorTableInfo(tableInfo)
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
    }
}