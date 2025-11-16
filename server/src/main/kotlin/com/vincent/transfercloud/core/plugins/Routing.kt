package com.vincent.transfercloud.core.plugins

import com.vincent.transfercloud.data.dto.UserInputDto
import com.vincent.transfercloud.data.dto.UserOutputDto
import com.vincent.transfercloud.data.repository.UserRepository
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
	routing {
		get("/") {
			call.respond("Hello From Vincent - Exposed Database ORM GOD")
		}

		get("/users") {
			call.respond(UserRepository.getAll())
		}

		get("/users/{id}") {
			val id = call.parameters["id"] ?: return@get call.respond("Missing or malformed id")
			val user: UserOutputDto? = UserRepository.getById(id)
			if (user == null) {
				call.respond("No user found with id $id")
			} else {
				call.respond(user)
			}
		}

		post("/users") {
			val user: UserInputDto = call.receive<UserInputDto>()
			println(user)
			UserRepository.create(user)
			call.respond("User created")
		}
	}
}