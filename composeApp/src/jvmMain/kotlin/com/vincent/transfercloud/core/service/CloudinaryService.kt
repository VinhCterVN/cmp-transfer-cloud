package com.vincent.transfercloud.core.service

import com.vincent.transfercloud.core.constant.client
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.streams.*
import java.io.File

object CloudinaryService {
	suspend fun uploadFile(
		file: File,
		cloudName: String = "dtf1ao1ds",
		uploadPreset: String = "cmp_transfer_cloud"
	) {
		val url = "https://api.cloudinary.com/v1_1/$cloudName/auto/upload"
		val contentType = when (file.extension.lowercase()) {
			"jpg", "jpeg" -> "image/jpeg"
			"png" -> "image/png"
			"gif" -> "image/gif"
			"mp4" -> "video/mp4"
			"mov" -> "video/quicktime"
			"pdf" -> "application/pdf"
			else -> "application/octet-stream"
		}
		val formData = MultiPartFormDataContent(
			formData {
				append("upload_preset", uploadPreset)
				append("file", file.inputStream().asInput(), Headers.build {
					append(HttpHeaders.ContentType, contentType)
					append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
				})
			}
		)
		try {
			val response = client.post(url) {
				setBody(formData)
				onUpload { bytesSentTotal, contentLength ->
					println("Uploaded $bytesSentTotal of $contentLength bytes")
				}
			}.bodyAsText()
			println(response)

		} catch (e: Exception) {
			println("Upload thất bại: ${e.message}")
			e.printStackTrace()
			null
		}
	}
}