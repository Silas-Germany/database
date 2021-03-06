package com.github.silasgermany.complexormprocessor.models

data class TableInfo(
    val columns: MutableList<Column>,
    val isRoot: Boolean,
    val superTable: String?
) {
    var tableName: String? = null
    set(value) {
        if (value?.contains('.') == true)
            field = value.split(".").last().replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
    }

    override fun toString() = "${TableInfo::class.java.simpleName}(columns=$columns, superTable=$superTable, tableName=$tableName)"
}
