package com.vincent.transfercloud.data.dto

import com.vincent.transfercloud.data.enum.SharePermission
import kotlinx.serialization.Serializable

@Serializable
data class FolderInputDto(
    val name: String,
    val ownerId: String,
    val parentId: String? = null
)

@Serializable
data class FolderOutputDto(
    val id: String,
    val name: String,
    val breadcrumb: List<BreadcrumbItem> = emptyList(),
    val ownerId: String,
    val parentId: String?,
    val createdAt: String,
    val updatedAt: String,
    val sharedAt: String? = null,
    val sharePermission: SharePermission? = null
)

@Serializable
data class FolderWithContentsDto(
    val folder: FolderOutputDto,
    val subfolders: List<FolderOutputDto>,
    val files: List<FileOutputDto>
)

@Serializable
data class FolderDownloadMetadata(
    val message: String,
    val folderName: String,
    val serverHost: String,
    val serverPort: Int,
)

@Serializable
data class BreadcrumbItem(
    val id: String,
    val name: String
)