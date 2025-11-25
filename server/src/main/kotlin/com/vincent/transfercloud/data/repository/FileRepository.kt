package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.dto.BreadcrumbItem
import com.vincent.transfercloud.data.dto.FileOutputDto
import com.vincent.transfercloud.data.enum.FileLocation
import com.vincent.transfercloud.data.enum.SharePermission
import com.vincent.transfercloud.data.repository.FolderRepository.getFolderBreadcrumb
import com.vincent.transfercloud.data.schema.Files
import com.vincent.transfercloud.data.schema.Shares
import com.vincent.transfercloud.utils.toFileOutputDto
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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
		val sharedFileIds = Shares.selectAll()
			.where { (Shares.sharedWithUserId eq ownerUuid) and (Shares.fileId.isNotNull()) }
			.mapNotNull { it[Shares.fileId]?.value }

		Files.selectAll().where { Files.id inList(sharedFileIds) }
			.map { it.toFileOutputDto(getFileBreadcrumb(it[Files.id].value, ownerUuid)) }
	}

	fun moveFile(id: String, targetParentId: String, ownerId: String): Int = transaction {
		val fileUuid = UUID.fromString(id)
		val targetParentUuid = UUID.fromString(targetParentId)
		val ownerUuid = UUID.fromString(ownerId)
		Files.update({ (Files.id eq fileUuid) and (Files.ownerId eq ownerUuid) }) {
			it[folderId] = targetParentUuid
		}
	}
}