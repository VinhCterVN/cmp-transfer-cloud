package com.vincent.transfercloud.core.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudinaryResponse(
	@SerialName("public_id")
	val publicId: String,
	@SerialName("version")
	val version: Long,
	@SerialName("signature")
	val signature: String? = null,
	@SerialName("width")
	val width: Int,
	@SerialName("height")
	val height: Int,
	@SerialName("format")
	val format: String,
	@SerialName("resource_type")
	val resourceType: String,
	@SerialName("created_at")
	val createdAt: String,
	@SerialName("bytes")
	val bytes: Long,
	@SerialName("type")
	val type: String,
	@SerialName("etag")
	val etag: String,
	@SerialName("placeholder")
	val placeholder: Boolean? = null,
	@SerialName("url")
	val url: String,
	@SerialName("secure_url")
	val secureUrl: String,
	@SerialName("folder")
	val folder: String? = null,
	@SerialName("original_filename")
	val originalFilename: String
)