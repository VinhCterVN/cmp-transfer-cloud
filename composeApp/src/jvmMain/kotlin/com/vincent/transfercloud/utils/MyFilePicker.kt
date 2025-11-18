package com.vincent.transfercloud.utils

import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFileChooser.APPROVE_OPTION

object MyFilePicker {
	fun pickFileFromDesktop(): File? {
		val chooser = JFileChooser();
		val result = chooser.showOpenDialog(null)

		return if (result == APPROVE_OPTION) {
			chooser.selectedFile
		} else null
	}
}