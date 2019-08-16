package com.github.silasgermany.complexorm

import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class ComplexOrmTableInfoTest {
	private val currentTableInfo: ComplexOrmTableInfoInterface = tableInfo

	@Test fun allColumnNamesGiven() {
		val allColumnNames = currentTableInfo.columnNames.flatMap { it.value.keys }
		assertTrue {
			currentTableInfo.normalColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			currentTableInfo.connectedColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			currentTableInfo.joinColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			currentTableInfo.reverseConnectedColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			currentTableInfo.reverseJoinColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
		assertTrue {
			currentTableInfo.specialConnectedColumns
				.all { it.value.all { columnInfo -> columnInfo.key in allColumnNames } }
		}
	}

	@Test fun allTableClassNamesGiven() {
		val allTableClassNames = currentTableInfo.basicTableInfo.keys
		assertTrue {
			currentTableInfo.normalColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			currentTableInfo.connectedColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			currentTableInfo.joinColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			currentTableInfo.reverseConnectedColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			currentTableInfo.reverseJoinColumns
				.all { it.key in allTableClassNames }
		}
		assertTrue {
			currentTableInfo.specialConnectedColumns
				.all { it.key in allTableClassNames }
		}
	}
}