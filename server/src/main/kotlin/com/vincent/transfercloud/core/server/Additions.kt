package com.vincent.transfercloud.core.server

import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.util.concurrent.TimeUnit

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

fun fileExtensionFromName(fileName: String): String {
	val lastDot = fileName.lastIndexOf('.')
	return if (lastDot != -1 && lastDot != fileName.length - 1) {
		fileName.substring(lastDot + 1)
	} else {
		""
	}
}

val storageDir = File(System.getProperty("user.dir"), "storage").apply { if (!exists()) mkdirs() }
val thumbnailDir = File(System.getProperty("user.dir"), "thumbnails").apply { if (!exists()) mkdirs() }
val decriptedDir = File(System.getProperty("user.dir"), "decrypted").apply { if (!exists()) mkdirs() }

fun createFileThumbnailBytes(fileName: String, ownerId: String?, encPath: String?, storagePath: String?): ByteArray? {
	val cacheFile = File(thumbnailDir, "${ownerId}_${fileName}_thumb.jpg")
	if (cacheFile.exists() && cacheFile.length() > 0) {
		println("Loading cached thumbnail for $fileName")
		return cacheFile.readBytes()
	}

	println("Creating thumbnail for $fileName")
	val path = storagePath ?: "${ownerId}/$encPath"
	val ext = fileExtensionFromName(fileName).lowercase()
	val decryptedFile = File(decriptedDir, "${ownerId}_${fileName}")

	try {
		decryptedFile.writeBytes(FileDecryptor.loadAndDecryptFile(path, KeyManager.getFixedSecretKeyFromEnv()))
	} catch (e: Exception) {
		println("Failed to decrypt file for thumbnail: ${e.message}")
		return null
	}

	try {
		val thumbnailBytes: ByteArray? = when (ext) {
			in listOf("mp4", "mov", "avi", "mkv", "wmv", "flv", "webm") -> {
				println("Generating video thumbnail for $fileName")
				val thumbnailFile = File(thumbnailDir, "temp_${ownerId}_${fileName}.jpg")
				val ffmpegCommand = listOf(
					"ffmpeg", "-i", decryptedFile.absolutePath,
					"-ss", "00:00:01", "-vframes", "1", "-q:v", "2",
					thumbnailFile.absolutePath, "-y"
				)
				val process = ProcessBuilder(ffmpegCommand).redirectErrorStream(true).start()
				process.waitFor(5, TimeUnit.SECONDS)

				if (thumbnailFile.exists() && thumbnailFile.length() > 0) {
					thumbnailFile.readBytes().also { thumbnailFile.delete() }
				} else null
			}

			in listOf("mp3", "wav", "flac", "aac", "ogg", "m4a") -> {
				try {
					java.util.logging.Logger.getLogger("org.jaudiotagger").level = java.util.logging.Level.OFF
					val audioFile = AudioFileIO.read(decryptedFile)
					val tag = audioFile.tag
					val artwork = tag?.firstArtwork

					if (artwork != null) {
						println("Found album art for $fileName (${artwork.mimeType})")
						artwork.binaryData
					} else {
						println("No album art found inside tag for $fileName")
						null
					}
				} catch (e: Exception) {
					println("Error reading audio tag: ${e.message}")
					null
				}
			}

			in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> {
				println("Using image file itself as thumbnail")
				decryptedFile.readBytes()
			}

			else -> null
		}

		if (thumbnailBytes != null && thumbnailBytes.isNotEmpty()) {
			println("Caching thumbnail for $fileName to ${cacheFile.name}")
			cacheFile.writeBytes(thumbnailBytes)
			return thumbnailBytes
		} else {
			println("Could not generate thumbnail for $fileName")
		}

	} catch (e: Exception) {
		println("Error generating thumbnail: ${e.message}")
		e.printStackTrace()
	} finally {
		if (decryptedFile.exists()) {
			decryptedFile.delete()
		}
	}

	return null
}