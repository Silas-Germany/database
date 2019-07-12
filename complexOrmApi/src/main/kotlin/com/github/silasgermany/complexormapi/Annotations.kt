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
annotation class ComplexOrmSpecialConnectedColumn(val connectedColumn: String)


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
annotation class ComplexOrmIndex(val group: Int = 1)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ComplexOrmDeleteCascade

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ComplexOrmDeleteRestrict

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ComplexOrmUniqueIndex(val group: Int)


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ComplexOrmReadAlways