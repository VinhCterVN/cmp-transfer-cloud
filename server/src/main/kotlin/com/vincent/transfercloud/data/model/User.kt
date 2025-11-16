package com.vincent.transfercloud.data.model

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID
import kotlin.time.ExperimentalTime

object Users : UUIDTable("users") {
	val fullName = varchar("full_name", 255)
	val email = varchar("email", 255).uniqueIndex()
	val avatarUrl = varchar("avatar_url", 500).nullable()
	val passwordHash = varchar("password_hash", 255)
	val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
	val isActive = bool("is_active").default(true)
}

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<UserEntity>(Users)

	var email by Users.email
	var passwordHash by Users.passwordHash
	var fullName by Users.fullName
	var avatarUrl by Users.avatarUrl

	@OptIn(ExperimentalTime::class)
	var createdAt by Users.createdAt
	var isActive by Users.isActive

	val ownedFiles by FileEntity referrersOn Files.ownerId
	val shares by ShareEntity referrersOn Shares.ownerId
}