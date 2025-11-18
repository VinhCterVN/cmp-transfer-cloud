package com.vincent.transfercloud.data.schema

import com.vincent.transfercloud.data.enum.FileLocation
import com.vincent.transfercloud.data.enum.SharePermission
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : UUIDTable("users") {
	val fullName = varchar("full_name", 255)
	val email = varchar("email", 255).uniqueIndex()
	val avatarUrl = varchar("avatar_url", 500).nullable()
	val isValid = bool("is_valid").default(true)
	val passwordHash = varchar("password_hash", 255)
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object Folders : UUIDTable("folders") {
	val name = varchar("name", 255)
	val ownerId = reference("owner_id", Users, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
	val parentId = reference("parent_id", Folders, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE).nullable()
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

object Files : UUIDTable("files") {
	val name = varchar("name", 255)
	val folderId = reference("folder_id", Folders, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
	val ownerId = reference("owner_id", Users, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
	val fileSize = long("file_size")
	val mimeType = varchar("mime_type", 100)
	val storagePath = varchar("storage_path", 500)
	val location = enumerationByName("location", 20, FileLocation::class).default(FileLocation.CLOUD)
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
	val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

object Shares : UUIDTable("shares") {
	val fileId = reference("file_id", Files, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
		.nullable()
	val folderId = reference("folder_id", Folders, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE).nullable()
	val ownerId = reference("owner_id", Users, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
	val sharedWithUserId = reference("shared_with_user_id", Users, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
	val permission = enumerationByName("permission", 20, SharePermission::class)
	val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object Activities : UUIDTable("activities") {
	val userId = reference("user_id", Users, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
	val action = varchar("action", 50)
	val fileId = reference("file_id", Files, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE).nullable()
	val folderId = reference("folder_id", Folders, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE).nullable()
	val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp)
}