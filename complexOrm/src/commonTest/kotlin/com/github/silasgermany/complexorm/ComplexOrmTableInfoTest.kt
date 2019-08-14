package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import kotlin.test.Test
import kotlin.test.assertTrue

class ComplexOrmTableInfoTest {
	val tableInfo: ComplexOrmTableInfoInterface = ComplexOrmTableInfo()

	@Test
	fun onlyTypeEnums() {
		val typeNames = ComplexOrmTypes.values().map { it.name }
		assertTrue {
			tableInfo.normalColumns
				.all { it.value.all { columnInfo -> columnInfo.value in typeNames } }
		}
	}

	@Test
	fun allColumnNamesGiven() {
		val allColumnNames = tableInfo.columnNames.flatMap { it.value.keys }
		assertTrue {
			tableInfo.normalColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			tableInfo.connectedColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			tableInfo.joinColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			tableInfo.reverseConnectedColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			tableInfo.reverseJoinColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			tableInfo.specialConnectedColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
	}

	@Test
	fun allTableClassNamesGiven() {
		val allTableClassNames = tableInfo.basicTableInfo.keys
		assertTrue {
			tableInfo.normalColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			tableInfo.connectedColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			tableInfo.joinColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			tableInfo.reverseConnectedColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			tableInfo.reverseJoinColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			tableInfo.specialConnectedColumns
				.all { it.key in allTableClassNames }
		}
	}
}