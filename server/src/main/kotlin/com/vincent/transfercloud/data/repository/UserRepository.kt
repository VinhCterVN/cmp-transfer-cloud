package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.dto.UserInputDto
import com.vincent.transfercloud.data.dto.UserOutputDto
import com.vincent.transfercloud.data.helper.EmailAlreadyExistsException
import com.vincent.transfercloud.data.helper.hashPassword
import com.vincent.transfercloud.data.schema.Folders
import com.vincent.transfercloud.data.schema.Users
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object UserRepository {
	fun createUser(user: UserInputDto): UserOutputDto = try {
		transaction {
			val id = Users.insertAndGetId {
				it[fullName] = user.fullName
				it[email] = user.email
				it[avatarUrl] = user.avatarUrl
				it[passwordHash] = hashPassword(user.password)
			}.value
			val rootFolderId = FileRepository.createRootFolder(id)

			UserOutputDto(
				id = id.toString(),
				fullName = user.fullName,
				email = user.email,
				avatarUrl = user.avatarUrl,
				rootFolderId = rootFolderId.toString(),
			)
		}
	} catch (e: ExposedSQLException) {
		val msg = e.cause?.message ?: e.message ?: ""

		if (msg.contains("unique", ignoreCase = true)) {
			throw EmailAlreadyExistsException()
		}

		throw e
	}

	fun getAll(): List<UserOutputDto> = transaction {
		Users.selectAll().map {
			UserOutputDto(
				id = it[Users.id].value.toString(),
				fullName = it[Users.fullName],
				email = it[Users.email],
				avatarUrl = it[Users.avatarUrl],
			)
		}
	}

	fun getByEmail(email: String): UserOutputDto? = transaction {
		val userRow = Users.selectAll().where { Users.email eq email }.singleOrNull() ?: return@transaction null
		val rootFolderId = Folders
			.selectAll().where { (Folders.ownerId eq userRow[Users.id]) and (Folders.parentId.isNull()) }
			.map { it[Folders.id].value }
			.firstOrNull()

		UserOutputDto(
			id = userRow[Users.id].value.toString(),
			fullName = userRow[Users.fullName],
			email = userRow[Users.email],
			avatarUrl = userRow[Users.avatarUrl],
			rootFolderId = rootFolderId?.toString()
		)
	}

	fun getById(uuid: UUID): UserOutputDto? = transaction {
		val userRow = Users.selectAll().where { Users.id eq uuid }.singleOrNull() ?: return@transaction null
		val rootFolderId = Folders
			.selectAll().where { (Folders.ownerId eq uuid) and (Folders.parentId.isNull()) }
			.map { it[Folders.id].value }
			.firstOrNull()

		UserOutputDto(
			id = userRow[Users.id].value.toString(),
			fullName = userRow[Users.fullName],
			email = userRow[Users.email],
			avatarUrl = userRow[Users.avatarUrl],
			rootFolderId = rootFolderId?.toString()
		)
	}
}

