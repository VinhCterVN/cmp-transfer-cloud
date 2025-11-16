@file:OptIn(ExperimentalTime::class)

package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.schema.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.time.ExperimentalTime

object FileRepository {

	fun createRootFolder(userId: UUID, folderName: String = "My Drive"): UUID {
		return transaction {
			Folders.insertAndGetId {
				it[name] = folderName
				it[ownerId] = userId
				it[parentId] = null
			}.value
		}
	}

	fun createFolder(userId: UUID, folderName: String, parentFolderId: UUID): UUID {
		return transaction {
			Folders.insertAndGetId {
				it[name] = folderName
				it[ownerId] = userId
				it[parentId] = parentFolderId
			}.value
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
