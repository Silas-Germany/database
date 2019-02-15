package com.github.silasgermany.complexormprocessor

data class TableInfo(
    val columns: MutableList<Column>,
    val superTable: String?
) {
    var tableName: String? = null
    set(value) {
        field = value?.split(".")?.last()?.replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2")?.toLowerCase()
    }

    override fun toString() = "${TableInfo::class.java.simpleName}(columns=$columns, superTable=$superTable, tableName=$tableName)"
}
