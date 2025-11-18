package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.schema.Files
import com.vincent.transfercloud.data.schema.Folders
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object FileRepository {
	/**
	 * Upload File
	 * @param fileSize [String]
	 * */
	fun createFile(
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

	fun deleteFile(fileId: String, ownerId: String): Boolean = transaction {
		val fileId = UUID.fromString(fileId)
		val ownerUuid = UUID.fromString(ownerId)
		val deletedRows = Files.deleteWhere { (Files.id eq fileId) and (Files.ownerId eq ownerUuid) }
		deletedRows > 0
	}
}