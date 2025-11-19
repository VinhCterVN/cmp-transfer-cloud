@file:OptIn(ExperimentalTime::class)

package com.vincent.transfercloud.core.plugins

import com.vincent.transfercloud.data.dto.*
import com.vincent.transfercloud.data.helper.DatabaseSeeder
import com.vincent.transfercloud.data.repository.AuthRepository
import com.vincent.transfercloud.data.repository.FileRepository
import com.vincent.transfercloud.data.repository.FolderRepository
import com.vincent.transfercloud.data.repository.UserRepository
import com.vincent.transfercloud.utils.json
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.util.*
import kotlin.time.ExperimentalTime

fun Application.configureRouting() {
	routing {
		get("/") {
			call.respondRedirect("/api")
		}

		get("/api") {
			call.respondText("Hello From Vincent - Exposed Database\n${LocalDateTime.now()}", contentType = ContentType.Text.Html)
		}
		get("/api/users") {
			call.respond(UserRepository.getAll())
		}

		post("/api/auth/login") {
			val req = call.receive<LoginRequest>()
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
		/**FILE MANIPULATION**/
		get("/api/folders/{folder_id}/{owner_id}") {
			val id = call.parameters["folder_id"] ?: return@get call.respond("Missing or malformed id")
			val ownerId = call.parameters["owner_id"] ?: return@get call.respond("Missing or malformed owner id")
			val folder = FolderRepository.getFolderById(id, ownerId)
			call.respond(
				GetFolderContentsRequestDto(
					folderId = id,
					status = if (folder != null) ResponseStatus.SUCCESS else ResponseStatus.ERROR,
					message = if (folder != null) "Folder found" else "Folder not found",
					data = folder
				)
			)
		}

		post("/api/folders") {
			val req = call.receive<CreateFolderRequest>()
			val res = FolderRepository.createFolder(req.ownerId, req.folderName, req.parentFolderId)
			call.respond(
				CreateFolderResponseDto(
					folder = res,
					status = if (res != null) ResponseStatus.SUCCESS else ResponseStatus.ERROR,
					message = if (res != null) "Folder created" else "Failed to create folder"
				)
			)
		}

		delete("/api/folders/{folder_id}/{owner_id}") {
			val id = call.parameters["folder_id"] ?: return@delete call.respond("Missing or malformed id")
			val ownerId = call.parameters["owner_id"] ?: return@delete call.respond("Missing or malformed owner id")
			val success = FolderRepository.deleteFolder(id, ownerId)
			if (success) {
				call.respondText("Folder deleted successfully", status = HttpStatusCode.OK)
			} else {
				call.respondText("Failed to delete folder", status = HttpStatusCode.InternalServerError)
			}
		}


		delete("/api/files/{file_id}/{owner_id}") {
			val id = call.parameters["file_id"] ?: return@delete call.respond("Missing or malformed id")
			val ownerId = call.parameters["owner_id"] ?: return@delete call.respond("Missing or malformed owner id")
			val success = FileRepository.deleteFile(id, ownerId)
			if (success.isNotEmpty()) {
				call.respondText("File deleted successfully", status = HttpStatusCode.OK)
			} else {
				call.respondText("Failed to delete file", status = HttpStatusCode.InternalServerError)
			}
		}

		get("/api/seed") {
			DatabaseSeeder.seed(0, 100, 200)
			call.respondText("Database seeded", contentType = ContentType.Text.Html)
		}
	}
}