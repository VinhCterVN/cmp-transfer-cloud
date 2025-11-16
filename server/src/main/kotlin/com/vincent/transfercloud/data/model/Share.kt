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

object Shares : UUIDTable("shares") {
	val fileId = reference("file_id", Files)
	val ownerId = reference("owner_id", Users)
	val sharedWithUserId = reference("shared_with_user_id", Users)
	val permission = varchar("permission", 20).default("view") // 'view', 'edit', 'comment'
	val shareLink = varchar("share_link", 100).nullable().uniqueIndex()
	val shareLinkPassword = varchar("share_link_password", 255).nullable()

	val isPublic = bool("is_public").default(false)

	val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

class ShareEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<ShareEntity>(Shares)

	var file by FileEntity referencedOn Shares.fileId
	var owner by UserEntity referencedOn Shares.ownerId
	var sharedWithUser by UserEntity referencedOn Shares.sharedWithUserId
	var permission by Shares.permission
	var shareLink by Shares.shareLink
	var shareLinkPassword by Shares.shareLinkPassword
	var isPublic by Shares.isPublic

	@OptIn(ExperimentalTime::class)
	var createdAt by Shares.createdAt

	fun hasEditPermission(): Boolean = permission == "edit"
	fun hasViewPermission(): Boolean = permission in listOf("view", "edit", "comment")
}
