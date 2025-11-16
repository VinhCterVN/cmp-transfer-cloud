@file:OptIn(ExperimentalTime::class)

package com.vincent.transfercloud.data.schema

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import kotlin.time.ExperimentalTime

object Users : UUIDTable("users") {
	val fullName = varchar("full_name", 255)
	val email = varchar("email", 255).uniqueIndex()
	val avatarUrl = varchar("avatar_url", 500).nullable()
	val passwordHash = varchar("password_hash", 255)
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object Folders : UUIDTable("folders") {
	val name = varchar("name", 255)
	val ownerId = reference("owner_id", Users)
	val parentId = reference("parent_id", Folders).nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

object Files : UUIDTable("files") {
	val name = varchar("name", 255)
	val folderId = reference("folder_id", Folders)
	val ownerId = reference("owner_id", Users)
	val fileSize = long("file_size")
	val mimeType = varchar("mime_type", 100)
	val storagePath = varchar("storage_path", 500)
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

object Shares : UUIDTable("shares") {
	val fileId = reference("file_id", Files).nullable()
	val folderId = reference("folder_id", Folders).nullable()
	val ownerId = reference("owner_id", Users)
	val sharedWithUserId = reference("shared_with_user_id", Users)
	val permission = enumerationByName("permission", 20, SharePermission::class)
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

enum class SharePermission {
	VIEW,
	EDIT,
	OWNER
}

enum class FileLocation {
	LOCAL,
	CLOUD
}

object Activities : UUIDTable("activities") {
	val userId = reference("user_id", Users)
	val action = varchar("action", 50)
	val fileId = reference("file_id", Files).nullable()
	val folderId = reference("folder_id", Folders).nullable()
	val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp)
}