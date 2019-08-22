package com.github.silasgermany.complexorm.otherFunctions

import com.github.silasgermany.complexorm.CommonFile
import com.github.silasgermany.complexorm.helper.CommonHelper
import kotlin.test.*

class CommonFileTest: CommonHelper() {

	private val commonFile = CommonFile("/tmp/test")

	@AfterTest fun tearDown() {
		commonFile.listFiles()?.forEach { it.delete() }
		commonFile.delete()
	}

	@Test fun testGetPath() {
		assertEquals("/tmp/test", commonFile.getPath())
		var child = CommonFile(commonFile, "test.txt")
		assertEquals("/tmp/test/test.txt", child.getPath())
		child = CommonFile("/tmp/test", "test.txt")
		assertEquals("/tmp/test/test.txt", child.getPath())
		child = CommonFile("/tmp/test/", "test.txt")
		assertEquals("/tmp/test/test.txt", child.getPath())
	}

	@Test fun testListFiles() {
		commonFile.mkdir()
		CommonFile(commonFile, "sub_folder_1").mkdir()
		CommonFile(commonFile, "sub_folder_2").mkdir()
		CommonFile(commonFile, "sub_folder_3").mkdir()
		val filePaths = commonFile.listFiles()?.mapTo(mutableSetOf()) { it.getPath() }
		val expectedPaths = listOf("sub_folder_1", "sub_folder_2", "sub_folder_3")
			.mapTo(mutableSetOf()) { commonFile.getPath() + '/' + it }
		assertEquals(expectedPaths, filePaths)
	}

	@Test fun testExists() {
		assertFalse(commonFile.exists())
		commonFile.mkdir()
		assertTrue(commonFile.exists())
	}
}
