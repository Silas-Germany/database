package com.github.silasgermany.complexormprocessor

import com.github.silasgermany.complexormapi.ComplexOrmSchemaInterface
import com.github.silasgermany.complexormapi.ComplexOrmTablesInterface
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class Processor(processingEnvironment: ProcessingEnvironment) {


    private val targetPackage = "com.github.silasgermany.complexorm"
    private val kaptKotlinGeneratedDir = processingEnvironment.options["kapt.kotlin.generated"]!!

    private val tableExtractor = TableInfoExtractor(processingEnvironment.messager, processingEnvironment.typeUtils)

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
                    .addProperty(tableInfoFileCreator.createNormalColumnsInfo())
                    .addProperty(tableInfoFileCreator.createConnectedColumnsInfo())
                    .addProperty(tableInfoFileCreator.createJoinColumnsInfo())
                    .addProperty(tableInfoFileCreator.createReverseJoinColumnsInfo())
                    .addProperty(tableInfoFileCreator.createReverseConnectedColumnsInfo())
                    .addProperty(tableInfoFileCreator.createConstructors())
                    .addProperty(tableInfoFileCreator.createBasicTableInfo())
                    .build()
            ).build()
        file.writeTo(File(kaptKotlinGeneratedDir))
    }
}