package com.vincent.transfercloud.utils

import com.vincent.transfercloud.core.constant.json
import kotlinx.serialization.Serializable
import java.io.File

object ThumbnailHelper {
	private val configFile = File("C:\\TransferCloud\\thumbnails_config.json")
	private val persistentDir = File("C:\\TransferCloud\\Thumbnails").apply {
		if (!exists()) mkdirs()
	}

	@Serializable
	data class ThumbnailConfig(
		val files: Map<String, String> = emptyMap()
	)

	fun save(files: Map<String, File>) {
		val config = ThumbnailConfig(
			files = files.mapValues { it.value.absolutePath }
		)
		configFile.writeText(json.encodeToString(config))
	}

	fun load(): Map<String, File> {
		if (!configFile.exists()) return emptyMap()

		return try {
			val config = json.decodeFromString<ThumbnailConfig>(configFile.readText())
			config.files.mapNotNull { (id, path) ->
				val file = File(path)
				if (file.exists()) id to file else null
			}.toMap()
		} catch (e: Exception) {
			println("Error loading thumbnail config: ${e.message}")
			emptyMap()
		}
	}

	fun clear() {
		if (configFile.exists()) configFile.delete()
		persistentDir.listFiles()?.forEach { it.delete() }
	}
}