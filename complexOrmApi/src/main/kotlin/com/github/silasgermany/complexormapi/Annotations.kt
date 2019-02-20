package com.github.silasgermany.complexormapi

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ComplexOrmAllTables


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Suppress("UNUSED")
annotation class ComplexOrmReverseJoinColumn(val connectedColumn: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Suppress("UNUSED")
annotation class ComplexOrmReverseConnectedColumn(val connectedColumn: String = "")


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Suppress("UNUSED")
annotation class ComplexOrmDefault(val value: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Suppress("UNUSED")
annotation class ComplexOrmProperty(val extra: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ComplexOrmUnique


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ComplexOrmReadAlways