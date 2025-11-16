package com.vincent.transfercloud.data.model

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class FileType { FILE, FOLDER }

object Files : UUIDTable("files") {
	val name = varchar("name", 255)
	val type = enumerationByName("type", 20, FileType::class)
	val mimeType = varchar("mime_type", 100).nullable() // 'image/jpeg', 'application/pdf', etc.
	val size = long("size").default(0)
	val parentId = reference("parent_id", Files).nullable()
	val ownerId = reference("owner_id", Users)
	val isStarred = bool("is_starred").default(false)
	val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

class FileEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<FileEntity>(Files)

	var name by Files.name
	var type by Files.type
	var mimeType by Files.mimeType
	var size by Files.size

	var parent by FileEntity optionalReferencedOn Files.parentId
	var owner by UserEntity referencedOn Files.ownerId

	var isStarred by Files.isStarred

	@OptIn(ExperimentalTime::class)
	var createdAt by Files.createdAt
	val children by FileEntity optionalReferrersOn Files.parentId
	val shares by ShareEntity referrersOn Shares.fileId
	val isFile get() = type == FileType.FILE
	val isFolder get() = type == FileType.FOLDER
}
