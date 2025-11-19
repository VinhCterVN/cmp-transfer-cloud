package com.vincent.transfercloud.utils

import kotlinx.serialization.json.Json

val json = Json {
	ignoreUnknownKeys = true
	encodeDefaults = true
	isLenient = true
}