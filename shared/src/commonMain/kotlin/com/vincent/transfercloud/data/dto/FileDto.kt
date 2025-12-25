package com.vincent.transfercloud.data.dto

import com.vincent.transfercloud.data.enum.FileLocation
import com.vincent.transfercloud.data.enum.SharePermission
import kotlinx.serialization.Serializable

@Serializable
data class FileInputDto(
	val name: String,
	val folderId: String,
	val ownerId: String,
	val fileSize: Long,
	val mimeType: String,
	val storagePath: String,
	val location: FileLocation = FileLocation.CLOUD
)

@Serializable
data class FileOutputDto(
	val id: String,
	val name: String,
	val folderId: String,
	val ownerId: String,
	val fileSize: Long,
	val breadcrumb: List<BreadcrumbItem> = emptyList(),
	val mimeType: String,
	val storagePath: String,
	val location: FileLocation,
	var hasThumbnail: Boolean = false,
	val createdAt: String,
	val updatedAt: String,
	val sharedAt: String? = null,
	val sharePermission: SharePermission? = null
)

@Serializable
data class FileEntry(
    val fileId: String,
    val storagePath: String,
    val entryPath: String
)