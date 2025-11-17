package com.vincent.transfercloud.data.dto

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
    val ownerId: String,
    val parentId: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class FolderWithContentsDto(
    val folder: FolderOutputDto,
    val subfolders: List<FolderOutputDto>,
    val files: List<FileOutputDto>
)