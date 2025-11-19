package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.dto.BreadcrumbItem
import com.vincent.transfercloud.data.dto.FileOutputDto
import com.vincent.transfercloud.data.enum.FileLocation
import com.vincent.transfercloud.data.repository.FolderRepository.getFolderBreadcrumb
import com.vincent.transfercloud.data.schema.Files
import com.vincent.transfercloud.utils.toFileOutputDto
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
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

			Files.selectAll().where { Files.id eq fileId }.singleOrNull()?.toFileOutputDto(getFileBreadcrumb(fileId, ownerUuid))
		}
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

}