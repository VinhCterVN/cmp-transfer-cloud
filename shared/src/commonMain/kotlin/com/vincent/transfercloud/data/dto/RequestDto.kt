package com.vincent.transfercloud.data.dto

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class LoginRequest(
	val email: String,
	val password: String
)

@Serializable
data class RegisterRequest(
	val fullName: String,
	val email: String,
	val password: String,
	val avatarUrl: String = "https://i.pravatar.cc/300"
)

@Serializable
data class CreateFolderRequest(
	val ownerId: String,
	val folderName: String,
	val parentFolderId: String
)

@Serializable
data class CreateFileRequest(
	val ownerId: String,
	val fileName: String,
	val parentFolderId: String,
	val fileSize: Long,
	val mimeType: String,
	val shareIds: List<String> = emptyList(),
	val data: ByteArray
)

@Serializable
data class UploadFolderRequest(
	val id: String = UUID.randomUUID().toString(),
	val ownerId: String,

)