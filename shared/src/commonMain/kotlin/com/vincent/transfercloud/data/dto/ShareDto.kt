package com.vincent.transfercloud.data.dto

import com.vincent.transfercloud.data.enum.SharePermission
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class ShareInputDto(
	val fileId: String,
	val ownerId: String,
	val sharedWithUserId: String,
)

@Serializable
data class ShareOutputDto(
	val id: String,
	val fileId: String,
	val ownerId: String,
	val sharedWithUserId: String,
)

@Serializable
data class FileSharesInfoDto(
	val fileId: String,
	val ownerId: String,
	val shares: List<ShareMetadata>
)

@Serializable
data class FolderSharesInfoDto(
	val folderId: String,
	val ownerId: String,
	val shares: List<ShareMetadata>
)

@Serializable
data class ShareMetadata(
	val sharedWithUserId: String,
	val sharedWithUserEmail: String,
	val permission: SharePermission,
	val sharedAt: String
)