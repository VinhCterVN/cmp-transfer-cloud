package com.vincent.transfercloud.core.server

import com.vincent.transfercloud.data.dto.FileOutputDto
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

object FileSummarizer {
	val client = HttpClient()
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	suspend fun request(file: FileOutputDto?): String = withContext(Dispatchers.IO) {
		if (file == null) return@withContext "File is null"

		val responseText = client.post("http://localhost:8000/summarize") {
			setBody(
				MultiPartFormDataContent(
					formData {
						append(
							key = "file",
							value = FileDecryptor.loadAndDecryptFile(
								file.storagePath,
								KeyManager.getFixedSecretKeyFromEnv()
							),
							headers = Headers.build {
								append(
									HttpHeaders.ContentDisposition,
									"form-data; name=\"file\"; filename=\"${file.name}\""
								)
								append(
									HttpHeaders.ContentType,
									"application/vnd.openxmlformats-officedocument.wordprocessingml.document"
								)
							}
						)
					}
				)
			)
		}.bodyAsText()
		println("Summarization response: $responseText")
		return@withContext responseText
	}
}