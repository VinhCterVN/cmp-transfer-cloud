package com.vincent.transfercloud.utils

import kotlinx.serialization.json.Json

val json = Json {
	prettyPrint = true
	ignoreUnknownKeys = true
	encodeDefaults = true
	isLenient = true
}