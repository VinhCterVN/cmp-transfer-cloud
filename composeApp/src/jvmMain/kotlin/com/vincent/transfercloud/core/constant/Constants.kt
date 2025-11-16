package com.vincent.transfercloud.core.constant

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val json = Json {
	prettyPrint = true
	ignoreUnknownKeys = true
	encodeDefaults = true
	isLenient = true
}
val client = HttpClient(CIO) {
	install(ContentNegotiation) {
		json(json)
	}
}