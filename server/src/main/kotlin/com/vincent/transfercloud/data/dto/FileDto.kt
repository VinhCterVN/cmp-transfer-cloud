package com.vincent.transfercloud.data.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class FileInputDto(
	val name: String,
	val size: Long,
	val mimeType: String? = null,
	val parentId: String? = null,   // ← Jackson/Kotlinx sẽ parse string -> UUID
	val isStarred: Boolean = false
)


data class FileOutputDto(
	val id: UUID,
	val name: String,
	val type: String,
	val mimeType: String?,
	val size: Long,
	val parentId: UUID?,
	val ownerId: UUID,
	val isStarred: Boolean,
	val createdAt: LocalDateTime
)