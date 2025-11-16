@file:OptIn(ExperimentalTime::class)

package com.vincent.transfercloud.data.schema

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class User(
    val id: Int,
    val email: String,
    val fullName: String,
    val avatarUrl: String?,
    val passwordHash: String,
    val createdAt: Instant
)

data class FileItem(
    val id: Int,
    val name: String,
    val folderId: Int,
    val ownerId: Int,
    val fileSize: Long,
    val mimeType: String,
    val storagePath: String,
    val createdAt: Instant,
	val updatedAt: Instant
)

data class FolderItem(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val parentId: Int?,
    val createdAt: Instant,
	val updatedAt: Instant
)