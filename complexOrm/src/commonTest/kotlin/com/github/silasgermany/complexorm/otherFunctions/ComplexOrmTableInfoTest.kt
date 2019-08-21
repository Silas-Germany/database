package com.github.silasgermany.complexorm.otherFunctions

import com.github.silasgermany.complexorm.helper.CommonHelper
import com.github.silasgermany.complexorm.tableInfo
import com.github.silasgermany.complexormapi.ComplexOrmTableInfoInterface
import com.github.silasgermany.complexormapi.ComplexOrmTypes
import kotlin.test.Test
import kotlin.test.assertTrue

class ComplexOrmTableInfoTest: CommonHelper() {
	private val currentTableInfo: ComplexOrmTableInfoInterface = tableInfo

	@Test fun onlyTypeEnums() {
		val typeNames = ComplexOrmTypes.values().map { it.name }
		assertTrue {
			currentTableInfo.normalColumns
				.all { it.value.all { columnInfo -> columnInfo.value in typeNames } }
		}
	}

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