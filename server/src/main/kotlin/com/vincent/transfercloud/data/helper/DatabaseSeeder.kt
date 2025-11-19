package com.vincent.transfercloud.data.helper

import com.vincent.transfercloud.data.dto.UserInputDto
import com.vincent.transfercloud.data.repository.UserRepository
import com.vincent.transfercloud.data.schema.Files
import com.vincent.transfercloud.data.schema.Folders
import com.vincent.transfercloud.data.schema.Users
import io.github.serpro69.kfaker.Faker
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.random.Random

object DatabaseSeeder {
	private val faker = Faker()

	fun seed(
		userCount: Int = 5,
		folderCount: Int = 50,
		fileCount: Int = 200
	) = transaction {
		seedUsers(userCount)
		seedFolders(folderCount)
		seedFiles(fileCount)
	}

	private fun seedUsers(count: Int): List<String> {
		val userIds = mutableListOf<String>()
//		UserRepository.createUser(UserInputDto(
//			fullName = "Vincent Tran",
//			email = "vincent@mail.com",
//			password = "abcbac"
//		))
		repeat(count) {
			val userId = UserRepository.createUser(UserInputDto(
				fullName = faker.name.name(),
				email = faker.internet.email(),
				password = faker.string.bothify("??##??##")
			))!!.id
			userIds.add(userId)
		}
		return userIds
	}

	private fun seedFolders(count: Int): List<UUID> {
		val folderIds = mutableListOf<UUID>()
		val userFolders = mutableMapOf<UUID, MutableList<UUID>>()
		val userRootFolders = Folders.selectAll()
			.where { Folders.parentId.isNull() }
			.associate {
				it[Folders.ownerId].value to it[Folders.id].value
			}
		userRootFolders.forEach { (userId, rootFolderId) ->
			userFolders[userId] = mutableListOf(rootFolderId)
			folderIds.add(rootFolderId)
		}
		val userIds = userRootFolders.keys.toList()
		repeat(count) {
			val userId = userIds.random()
			val userFolderList = userFolders[userId]!!
			val parentFolderId = if (Random.nextFloat() > 0.3 && userFolderList.size > 1) {
				userFolderList.random()
			} else {
				userFolderList.first() // Root folder
			}
			val folderId = Folders.insertAndGetId {
				it[name] = generateFolderName()
				it[ownerId] = userId
				it[this.parentId] = parentFolderId
			}.value

			userFolderList.add(folderId)
			folderIds.add(folderId)
		}

		return folderIds
	}

	private fun seedFiles(count: Int): List<UUID> {
		val userIds = Users.selectAll().map {
			it[Users.id]
		}
		val folderIds = Folders.selectAll().map {
			it[Folders.id]
		}
		val fileIds = mutableListOf<UUID>()
		val mimeTypes = listOf(
			"application/pdf",
			"image/jpeg",
			"image/png",
			"image/gif",
			"video/mp4",
			"audio/mpeg",
			"text/plain",
			"application/zip",
			"application/vnd.ms-excel",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation"
		)

		repeat(count) {
			val folderId = folderIds.random()
			val userId = userIds.random()
			val mimeType = mimeTypes.random()
			val extension = getExtensionFromMimeType(mimeType)
			val fileId = Files.insertAndGetId {
				it[name] = "${
					faker.file.extension()
						.substringBeforeLast(".")
				}.$extension"
				it[this.folderId] = folderId
				it[ownerId] = userId
				it[fileSize] = Random.nextLong(1024, 100_000_000) // 1KB to 100MB
				it[this.mimeType] = mimeType
				it[storagePath] = "/storage/${UUID.randomUUID()}.$extension"
			}.value

			fileIds.add(fileId)
		}

		return fileIds
	}

	private fun generateFolderName(): String {
		val folderNames = listOf(
			"Documents",
			"Projects",
			"Photos",
			"Videos",
			"Music",
			"Downloads",
			"Work",
			"Personal",
			"Archives",
			"Backup"
		)

		return if (Random.nextFloat() > 0.5) {
			folderNames.random()
		} else {
			faker.address.city()
		}
	}

	private fun getExtensionFromMimeType(mimeType: String): String {
		return when (mimeType) {
			"application/pdf" -> "pdf"
			"image/jpeg" -> "jpg"
			"image/png" -> "png"
			"image/gif" -> "gif"
			"video/mp4" -> "mp4"
			"audio/mpeg" -> "mp3"
			"text/plain" -> "txt"
			"application/zip" -> "zip"
			"application/vnd.ms-excel" -> "xls"
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
			"application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "pptx"
			else -> "bin"
		}
	}
}

fun main() {
	DatabaseSeeder.seed(5, 100, 200)
}