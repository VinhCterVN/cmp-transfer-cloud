package com.vincent.transfercloud.utils

import java.io.File

fun File.isImage(): Boolean {
	val lower = name.lowercase()
	return lower.endsWith(".jpg") ||
			lower.endsWith(".jpeg") ||
			lower.endsWith(".png") ||
			lower.endsWith(".gif") ||
			lower.endsWith(".webp")
}

fun File.isVideo(): Boolean {
	val lower = name.lowercase()
	return lower.endsWith(".mp4") ||
			lower.endsWith(".mkv") ||
			lower.endsWith(".avi") ||
			lower.endsWith(".mov") ||
			lower.endsWith(".wmv") ||
			lower.endsWith(".flv") ||
			lower.endsWith(".webm")
}

fun File.isAudio(): Boolean {
	val lower = name.lowercase()
	return lower.endsWith(".mp3") ||
			lower.endsWith(".wav") ||
			lower.endsWith(".flac") ||
			lower.endsWith(".aac") ||
			lower.endsWith(".ogg") ||
			lower.endsWith(".m4a")
}
