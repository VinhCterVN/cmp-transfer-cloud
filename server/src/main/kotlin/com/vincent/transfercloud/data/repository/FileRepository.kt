package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.dto.BreadcrumbItem
import com.vincent.transfercloud.data.dto.FileOutputDto
import com.vincent.transfercloud.data.dto.ShareMetadata
import com.vincent.transfercloud.data.enum.FileLocation
import com.vincent.transfercloud.data.enum.SharePermission
import com.vincent.transfercloud.data.repository.FolderRepository.getFolderBreadcrumb
import com.vincent.transfercloud.data.schema.Files
import com.vincent.transfercloud.data.schema.Shares
import com.vincent.transfercloud.data.schema.Users
import com.vincent.transfercloud.utils.toFileOutputDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object FileRepository {
	/**
	 * Upload File
	 * @param fileSize [String]
	 * */
	fun createFile(
		fileName: String,
		parentFolderId: String,
		ownerId: String,
		fileSize: Long,
		mimeType: String,
		storagePath: String,
		shareIds: List<String> = emptyList(),
		location: FileLocation = FileLocation.LOCAL,
	): FileOutputDto? {
		return transaction {
			val ownerUuid = UUID.fromString(ownerId)
			val parentUuid = UUID.fromString(parentFolderId)
			val fileId = Files.insertAndGetId {
				it[Files.name] = fileName
				it[Files.folderId] = parentUuid
				it[Files.ownerId] = ownerUuid
				it[Files.fileSize] = fileSize
				it[Files.mimeType] = mimeType
				it[Files.storagePath] = storagePath
				it[Files.location] = location
			}.value

			Shares.batchInsert(shareIds) { shareId ->
				this[Shares.fileId] = fileId
				this[Shares.ownerId] = ownerUuid
				this[Shares.sharedWithUserId] = UUID.fromString(shareId)
				this[Shares.permission] = SharePermission.VIEW
			}

			Files.selectAll().where { Files.id eq fileId }.singleOrNull()?.toFileOutputDto(getFileBreadcrumb(fileId, ownerUuid))
		}
	}

	fun renameFile(id: String, data: String, ownerId: String): Boolean = transaction {
		val fileUuid = UUID.fromString(id)
		val ownerUuid = UUID.fromString(ownerId)
		val updatedRows = Files.update({ (Files.id eq fileUuid) and (Files.ownerId eq ownerUuid) }) {
			it[name] = data
		}
		updatedRows > 0
	}

	fun getFileById(fileId: String, ownerId: String): FileOutputDto? = transaction {
		val fileUuid = UUID.fromString(fileId)
		val ownerUuid = UUID.fromString(ownerId)
		Files.selectAll()
			.where { (Files.id eq fileUuid) and (Files.ownerId eq ownerUuid) }
			.singleOrNull()?.toFileOutputDto(getFileBreadcrumb(fileUuid, ownerUuid))
	}

	fun deleteFile(fileId: String, ownerId: String): String = transaction {
		val fileId = UUID.fromString(fileId)
		val ownerUuid = UUID.fromString(ownerId)
		val path = Files.selectAll().where { (Files.id eq fileId) and (Files.ownerId eq ownerUuid) }
			.singleOrNull()?.get(Files.storagePath) ?: return@transaction ""
		Files.deleteWhere { (Files.id eq fileId) and (Files.ownerId eq ownerUuid) }
		path
	}

	fun getFileBreadcrumb(fileId: UUID, ownerId: UUID): List<BreadcrumbItem> = transaction {
		val file = Files.selectAll()
			.where { (Files.id eq fileId) and (Files.ownerId eq ownerId) }
			.singleOrNull() ?: return@transaction emptyList()
		val folderId = file[Files.folderId].value
		getFolderBreadcrumb(folderId, ownerId)
	}

	fun getFilesSharedWithUser(ownerId: String) = transaction {
		val ownerUuid = UUID.fromString(ownerId)

		(Files innerJoin Shares)
			.selectAll()
			.where { (Shares.sharedWithUserId eq ownerUuid) and (Shares.fileId.isNotNull()) }
			.map { row ->
				val sharedTime = row[Shares.createdAt].toString()
				row.toFileOutputDto(
					breadcrumb = emptyList(),
					sharedAt = sharedTime,
					sharePermission = row[Shares.permission]
				)
			}
	}


	fun moveFile(id: String, targetParentId: String, ownerId: String): Int = transaction {
		val fileUuid = UUID.fromString(id)
		val targetParentUuid = UUID.fromString(targetParentId)
		val ownerUuid = UUID.fromString(ownerId)
		Files.update({ (Files.id eq fileUuid) and (Files.ownerId eq ownerUuid) }) {
			it[folderId] = targetParentUuid
		}
	}

	fun getFileSharedInfo(fileId: String, ownerId: String) = transaction {
		val fileUuid = UUID.fromString(fileId)
		val ownerUuid = UUID.fromString(ownerId)

		Shares.selectAll()
			.where { (Shares.fileId eq fileUuid) and (Shares.ownerId eq ownerUuid) }
			.map {
				val sharedWithUserEmail = Users.selectAll()
					.where { Users.id eq it[Shares.sharedWithUserId] }
					.singleOrNull()?.get(Users.email) ?: ""
				ShareMetadata(
					sharedWithUserId = it[Shares.sharedWithUserId].toString(),
					sharedWithUserEmail = sharedWithUserEmail,
					permission = it[Shares.permission],
					sharedAt = it[Shares.createdAt].toString()
				)
			}
	}

}