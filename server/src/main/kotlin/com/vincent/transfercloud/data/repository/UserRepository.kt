package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.dto.UserInputDto
import com.vincent.transfercloud.data.dto.UserOutputDto
import com.vincent.transfercloud.data.model.UserEntity
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

object UserRepository {
	fun create(user: UserInputDto) = transaction {
		UserEntity.new {
			fullName = user.fullName
			avatarUrl = user.avatarUrl
			email = user.email
			passwordHash = hashPassword(user.password)
		}
	}

	fun getAll(): List<UserOutputDto> = transaction { UserEntity.all().map { it.toOutputDto() } }

	fun getById(id: String): UserOutputDto? = transaction {
		val entity = UserEntity.findById(UUID.fromString(id))
		entity?.toOutputDto()
	}


	fun update(id: String, user: UserInputDto): UserOutputDto? = transaction {
		val uuid = runCatching { UUID.fromString(id) }.getOrNull()
		val entity = uuid?.let { UserEntity.findById(it) }
		entity?.fullName = user.fullName
		entity?.avatarUrl = user.avatarUrl
		entity?.email = user.email
		entity?.passwordHash = hashPassword(user.password)
		entity?.toOutputDto()
	}


	fun delete(id: String) = transaction {
		val uuid = runCatching { UUID.fromString(id) }.getOrNull()
		val entity = uuid?.let { UserEntity.findById(it) }
		entity?.delete()
	}

	private fun UserEntity.toOutputDto(): UserOutputDto {
		return UserOutputDto(
			id = this.id.value.toString(),
			fullName = this.fullName,
			avatarUrl = this.avatarUrl,
			email = this.email
		)
	}

	private fun hashPassword(password: String): String {
		val salt = BCrypt.gensalt(12)
		return BCrypt.hashpw(password, salt)
	}

	private fun verifyPassword(password: String, hashedPassword: String): Boolean {
		return BCrypt.checkpw(password, hashedPassword)
	}
}
