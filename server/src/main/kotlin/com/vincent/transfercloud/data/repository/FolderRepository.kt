@file:OptIn(ExperimentalTime::class)

package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.dto.*
import com.vincent.transfercloud.data.enum.SharePermission
import com.vincent.transfercloud.data.schema.Files
import com.vincent.transfercloud.data.schema.Folders
import com.vincent.transfercloud.data.schema.Shares
import com.vincent.transfercloud.data.schema.Users
import com.vincent.transfercloud.helper.getFileHasThumbnail
import com.vincent.transfercloud.utils.toFileOutputDto
import com.vincent.transfercloud.utils.toFolderOutputDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.time.ExperimentalTime

object FolderRepository {
	fun getFolderById(folderId: String, ownerId: String): FolderWithContentsDto? = transaction {
		val folderUuid = UUID.fromString(folderId)
		val ownerUuid = UUID.fromString(ownerId)
		val folder = Folders.selectAll()
			.where { (Folders.id eq folderUuid) and (Folders.ownerId eq ownerUuid) }
			.singleOrNull()?.toFolderOutputDto(getFolderBreadcrumb(UUID.fromString(folderId), UUID.fromString(ownerId)))
			?: return@transaction null
		val subfolders = Folders.selectAll()
			.where { (Folders.parentId eq folderUuid) and (Folders.ownerId eq ownerUuid) }
			.map { it.toFolderOutputDto(getFolderBreadcrumb(UUID.fromString(folderId), UUID.fromString(ownerId))) }
		val subfiles = Files.selectAll()
			.where { (Files.folderId eq folderUuid) and (Files.ownerId eq ownerUuid) }
			.map { it.toFileOutputDto(getFolderBreadcrumb(UUID.fromString(folderId), UUID.fromString(ownerId))) }

		FolderWithContentsDto(
			folder = folder,
			subfolders = subfolders,
			files = subfiles.map { it.copy(hasThumbnail = getFileHasThumbnail(it.name)) }
		)
	}

	fun getFolderBreadcrumb(folderId: UUID, ownerId: UUID): List<BreadcrumbItem> = transaction {
		val breadcrumb = mutableListOf<BreadcrumbItem>()
		var currentFolderId: UUID? = folderId

		while (currentFolderId != null) {
			val folder = Folders.selectAll()
				.where { (Folders.id eq currentFolderId) and (Folders.ownerId eq ownerId) }
				.singleOrNull() ?: break

			breadcrumb.add(0, BreadcrumbItem(folder[Folders.id].value.toString(), folder[Folders.name])) // Thêm vào đầu list
			currentFolderId = folder[Folders.parentId]?.value
		}

		breadcrumb
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
				it[Folders.name] = folderName
				it[Folders.ownerId] = ownerUuid
				it[Folders.parentId] = parentUuid
			}.value

			Folders.selectAll()
				.where { Folders.id eq folderId }
				.singleOrNull()?.toFolderOutputDto(getFolderBreadcrumb(folderId, UUID.fromString(userId)))
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

	fun deleteFolder(id: String, ownerId: String): Boolean = transaction {
		val folderUuid = UUID.fromString(id)
		val ownerUuid = UUID.fromString(ownerId)
		val deletedRows = Folders.deleteWhere { (Folders.id eq folderUuid) and (Folders.ownerId eq ownerUuid) }
		deletedRows > 0
	}

	fun getFoldersSharedWithUser(ownerId: String) = transaction {
		val ownerUuid = UUID.fromString(ownerId)
		val sharedFolderIds = Shares.selectAll()
			.where { (Shares.sharedWithUserId eq ownerUuid) and (Shares.folderId.isNotNull()) }
			.mapNotNull { it[Shares.folderId]?.value }

		Folders.selectAll()
			.where { Folders.id inList sharedFolderIds }
			.map { it.toFolderOutputDto(getFolderBreadcrumb(it[Folders.id].value, ownerUuid)) }
	}

	fun moveFolder(id: String, targetParentId: String, ownerId: String) = transaction {
		val folderUuid = UUID.fromString(id)
		val targetParentUuid = UUID.fromString(targetParentId)
		val ownerUuid = UUID.fromString(ownerId)
		Folders.update({ (Folders.id eq folderUuid) and (Folders.ownerId eq ownerUuid) }) {
			it[parentId] = targetParentUuid
		}
	}

	fun getFolderSharedInfo(folderId: String, ownerId: String) = transaction {
		val folderUuid = UUID.fromString(folderId)
		val ownerUuid = UUID.fromString(ownerId)
		Shares.selectAll()
			.where { (Shares.folderId eq folderUuid) and (Shares.ownerId eq ownerUuid) }
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

	fun getAllFilesWithCTE(rootFolderId: UUID): List<FileEntry> = transaction {
		// 1. Dùng tham số trực tiếp trong chuỗi (nhưng nhớ ép kiểu ::uuid cho chuẩn PostgreSQL)
		val sql = """
      WITH RECURSIVE folder_tree AS (
          SELECT id, name, CAST(name AS TEXT) as relative_path 
          FROM folders 
          WHERE id = '$rootFolderId'::uuid
      
          UNION ALL
          
          SELECT f.id, f.name, CAST(ft.relative_path || '/' || f.name AS TEXT) 
          FROM folders f
          INNER JOIN folder_tree ft ON f.parent_id = ft.id
      )
      SELECT 
          files.id as file_id,
          files.storage_path,
          files.name as file_name,
          folder_tree.relative_path
      FROM files
      INNER JOIN folder_tree ON files.folder_id = folder_tree.id;
      """.trimIndent()
		val result = TransactionManager.current().exec(
			stmt = sql,
			explicitStatementType = StatementType.SELECT // <--- QUAN TRỌNG: Dòng này sửa lỗi của bạn
		) { rs ->
			val results = mutableListOf<FileEntry>()
			while (rs.next()) {
				val fileName = rs.getString("file_name")
				val folderPath = rs.getString("relative_path")
				val fullZipPath = "$folderPath/$fileName"

				results.add(
					FileEntry(
						fileId = rs.getString("file_id"),
						storagePath = rs.getString("storage_path"),
						entryPath = fullZipPath
					)
				)
			}
			results
		}
		// Nếu exec trả về null thì trả về list rỗng
		result ?: emptyList()
	}
}
