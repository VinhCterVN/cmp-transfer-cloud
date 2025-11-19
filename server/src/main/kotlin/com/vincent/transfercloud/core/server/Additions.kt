package com.vincent.transfercloud.core.server

import java.io.File

fun File.fileNameWithoutExtension(): String {
	val name = this.name
	val lastDotIndex = name.lastIndexOf('.')
	return if (lastDotIndex != -1) {
		name.substring(0, lastDotIndex)
	} else {
		name
	}
}

fun fileNameWithoutExtensionFromPath(filePath: String): String {
	val lastDot = filePath.lastIndexOf('.')
	return if (lastDot != -1) {
		filePath.substring(0, lastDot)
	} else {
		filePath
	}
}