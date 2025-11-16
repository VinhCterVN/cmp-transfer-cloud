package com.vincent.transfercloud.data.repository

import com.vincent.transfercloud.data.dto.UserInputDto
import com.vincent.transfercloud.data.dto.UserOutputDto
import com.vincent.transfercloud.data.helper.verifyPassword
import com.vincent.transfercloud.data.schema.Users
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object AuthRepository {
	fun login(email: String, password: String): UserOutputDto? = transaction {
		val userRow = Users
			.selectAll()
			.where { Users.email eq email }
			.singleOrNull() ?: return@transaction null
		val hashFromDb = userRow[Users.passwordHash]
		if (!verifyPassword(password, hashFromDb)) {
			return@transaction null
		}

		UserRepository.getByEmail(email)
	}

	fun register(inputDto: UserInputDto): UserOutputDto = transaction {
		UserRepository.createUser(inputDto)
	}
}

