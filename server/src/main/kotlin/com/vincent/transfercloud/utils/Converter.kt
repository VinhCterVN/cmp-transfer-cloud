package com.vincent.transfercloud.utils

import com.vincent.transfercloud.data.dto.FileOutputDto
import com.vincent.transfercloud.data.dto.FolderOutputDto
import com.vincent.transfercloud.data.schema.Files
import com.vincent.transfercloud.data.schema.Folders
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toFolderOutputDto() = FolderOutputDto(
	id = this[Folders.id].value.toString(),
	name = this[Folders.name],
	ownerId = this[Folders.ownerId].value.toString(),
	parentId = this[Folders.parentId]?.toString(),
	createdAt = this[Folders.createdAt].toString(),
	updatedAt = this[Folders.updatedAt].toString()
)

fun ResultRow.toFileOutputDto() = FileOutputDto(
	id = this[Files.id].value.toString(),
	name = this[Files.name],
	folderId = this[Files.folderId].toString(),
	ownerId = this[Files.ownerId].toString(),
	fileSize = this[Files.fileSize],
	mimeType = this[Files.mimeType],
	storagePath = this[Files.storagePath],
	location = this[Files.location],
	createdAt = this[Files.createdAt].toString(),
	updatedAt = this[Files.updatedAt].toString()
)