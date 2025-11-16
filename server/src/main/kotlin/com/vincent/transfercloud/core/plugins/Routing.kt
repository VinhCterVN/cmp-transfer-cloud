@file:OptIn(ExperimentalTime::class)

package com.vincent.transfercloud.core.plugins

import com.vincent.transfercloud.data.dto.LoginRequestDto
import com.vincent.transfercloud.data.dto.UserInputDto
import com.vincent.transfercloud.data.dto.UserOutputDto
import com.vincent.transfercloud.data.repository.AuthRepository
import com.vincent.transfercloud.data.repository.UserRepository
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

val json = Json {
	prettyPrint = true
	isLenient = true
	ignoreUnknownKeys = true
}

fun Application.configureRouting() {
	routing {
		get("/") {
			call.respondRedirect("/api")
		}

		get("/api") {
			call.respondText("Hello From Vincent - Exposed Database\n${Clock.System.now()}", contentType = ContentType.Text.Html)
		}
		get("/api/users") {
			call.respond(UserRepository.getAll())
		}

		post("/api/auth/login") {
			val req = call.receive<LoginRequestDto>()
			val res = AuthRepository.login(req.email, req.password)
			call.respondText(json.encodeToString(res), ContentType.Text.Plain)
		}

		post("/api/auth/register") {
			val req = call.receive<UserInputDto>()
			val res = AuthRepository.register(req)
			call.respondText(json.encodeToString(res), ContentType.Text.Plain)
		}

		get("/api/users/{id}") {
			val id = call.parameters["id"] ?: return@get call.respond("Missing or malformed id")
			val user: UserOutputDto? = UserRepository.getById(UUID.fromString(id))
			if (user == null) {
				call.respond("No user found with id $id")
			} else {
				call.respond(user)
			}
		}

		post("/api/users") {
			val user: UserInputDto = call.receive<UserInputDto>()
			println(user)
			UserRepository.createUser(user)
			call.respond("User created")
		}
	}
}