package com.vincent.transfercloud.helper

private val thumbnailExts = setOf(
    ".jpg", ".jpeg", ".png", ".gif", ".webp",
    ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm",
    ".mp3", ".wav", ".flac", ".aac", ".ogg", ".m4a"
)

fun getFileHasThumbnail(fileName: String): Boolean {
    return thumbnailExts.any { fileName.endsWith(it, ignoreCase = true) }
}
