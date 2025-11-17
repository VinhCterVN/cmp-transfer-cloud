@file:OptIn(ExperimentalTime::class)

package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.dto.FolderOutputDto
import com.vincent.transfercloud.data.dto.FolderWithContentsDto
import com.vincent.transfercloud.data.enum.SharePermission
import com.vincent.transfercloud.data.schema.Files
import com.vincent.transfercloud.data.schema.Folders
import com.vincent.transfercloud.data.schema.Shares
import com.vincent.transfercloud.utils.toFileOutputDto
import com.vincent.transfercloud.utils.toFolderOutputDto
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.time.ExperimentalTime

object FileRepository {
	fun getFolderById(folderId: String, ownerId: String): FolderWithContentsDto? = transaction {
		val folderUuid = UUID.fromString(folderId)
		val ownerUuid = UUID.fromString(ownerId)
		val folder = Folders.selectAll()
			.where { (Folders.id eq folderUuid) and (Folders.ownerId eq ownerUuid) }
			.singleOrNull()?.toFolderOutputDto() ?: return@transaction null
		val subfolders = Folders.selectAll()
			.where { (Folders.parentId eq folderUuid) and (Folders.ownerId eq ownerUuid) }
			.map { it.toFolderOutputDto() }
		val subfiles = Files.selectAll()
			.where { (Files.folderId eq folderUuid) and (Files.ownerId eq ownerUuid) }
			.map { it.toFileOutputDto() }

		FolderWithContentsDto(
			folder = folder,
			subfolders = subfolders,
			files = subfiles
		)
	}

	fun createRootFolder(userId: UUID, folderName: String = "My Drive"): UUID {
		return transaction {
			Folders.insertAndGetId {
				it[name] = folderName
				it[ownerId] = userId
				it[parentId] = null
			}.value
		}
	}

	fun createFolder(userId: String, folderName: String, parentFolderId: String): FolderOutputDto? {
		return transaction {
			val ownerUuid = UUID.fromString(userId)
			val parentUuid = UUID.fromString(parentFolderId)
			val folderId = Folders.insertAndGetId {
				it[name] = folderName
				it[ownerId] = ownerUuid
				it[parentId] = parentUuid
			}.value

			Folders.selectAll()
				.where { Folders.id eq folderId }
				.singleOrNull()?.toFolderOutputDto()
		}
	}

	// Upload file
	fun uploadFile(
		fileName: String,
		folderId: UUID,
		ownerId: UUID,
		fileSize: Long,
		mimeType: String,
		storagePath: String
	): UUID {
		return transaction {
			Files.insertAndGetId {
				it[name] = fileName
				it[Files.folderId] = folderId
				it[Files.ownerId] = ownerId
				it[Files.fileSize] = fileSize
				it[Files.mimeType] = mimeType
				it[Files.storagePath] = storagePath
			}.value
		}
	}

	fun shareFolder(folderId: UUID, ownerId: UUID, sharedWithUserId: UUID, permission: SharePermission): UUID {
		return transaction {
			Shares.insertAndGetId {
				it[Shares.folderId] = folderId
				it[Shares.ownerId] = ownerId
				it[Shares.sharedWithUserId] = sharedWithUserId
				it[Shares.permission] = permission
			}.value
		}
	}

	fun deleteFile(fileId: UUID, userId: UUID): Boolean {
		return transaction {
			val deleted = Files.deleteWhere {
				(Files.id eq fileId) and (ownerId eq userId)
			}
			if (deleted > 0) {
				Shares.deleteWhere { Shares.fileId eq fileId }
			}

			deleted > 0
		}
	}
}
