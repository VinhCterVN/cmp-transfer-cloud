package com.vincent.transfercloud.core.constant

import com.vincent.transfercloud.SERVER_URL
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
	isLenient = true
	encodeDefaults = true
	ignoreUnknownKeys = true
	allowTrailingComma = true
}
val client = HttpClient(CIO) {
	install(ContentNegotiation) {
		json(json)
	}
	install(Logging) { level = LogLevel.INFO }
}
const val APP_URL = SERVER_URL