package com.vincent.transfercloud.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import java.io.File

object MyFilePicker {
	suspend fun pickFileFromDesktop(): File? {
		return FileKit.openFilePicker(
			title = "Select File",
		)?.file
	}
}