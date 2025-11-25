package com.vincent.transfercloud.ui.state

import org.jetbrains.compose.resources.DrawableResource
import transfercloud.composeapp.generated.resources.*

fun getFileIcon(fileName: String): DrawableResource {
	val extension = fileName.substringAfterLast('.', "").lowercase()
	return when (extension) {
		"jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "tiff" -> Res.drawable.mdi__image
		"mp4", "mkv", "avi", "mov", "wmv", "flv", "webm" -> Res.drawable.mdi__file_video
		"mp3", "wav", "flac", "aac", "ogg" -> Res.drawable.mdi__file_music
		"pdf" -> Res.drawable.mdi__file_pdf_box
		"doc", "docx", "txt", "odt", "rtf" -> Res.drawable.material_icon_theme__document
		"apk" -> Res.drawable.material_symbols__apk_document
		"exe" -> Res.drawable.carbon__executable_program
		"zip", "rar", "7z", "tar", "gz" -> Res.drawable.mdi__zip_box
		else -> Res.drawable.mdi__file
	}
}