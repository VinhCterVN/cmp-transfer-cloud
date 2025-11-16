package com.vincent.transfercloud.core.model

import kotlinx.serialization.Serializable

@Serializable
data class File(
	val name: String,
	val size: Long,
	val type: String,
)