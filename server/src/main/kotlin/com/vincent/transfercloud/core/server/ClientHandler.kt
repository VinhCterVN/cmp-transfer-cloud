package com.vincent.transfercloud.core.server

import com.vincent.transfercloud.data.dto.*
import com.vincent.transfercloud.data.repository.AuthRepository
import com.vincent.transfercloud.data.repository.FileRepository
import com.vincent.transfercloud.data.repository.FolderRepository
import com.vincent.transfercloud.data.repository.UserRepository
import com.vincent.transfercloud.utils.json
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import java.io.File
import java.util.*

class ClientHandler(
	private val socket: Socket,
	private val server: Server,
	private val scope: CoroutineScope
) {
	private val receiveChannel = socket.openReadChannel()
	private val sendChannel = socket.openWriteChannel(autoFlush = true)
	private var userId: String? = null

	fun start() = scope.launch(Dispatchers.IO) {
		try {
			var line: String? = ""
			while (receiveChannel.readUTF8Line().also { line = it } != null && sendChannel.isClosedForWrite.not()) {
				val text = line!!.trim()
				if (text.isEmpty()) continue
				try {
					val req = json.decodeFromString<SocketRequest>(text)
					handleRequest(req)
				} catch (e: Exception) {
					send(
						SocketResponse(
							status = ResponseStatus.ERROR,
							message = "Invalid request: ${e.message}"
						)
					)
				}
			}
		} catch (e: Exception) {
			println("ClientHandler error: ${e.javaClass.simpleName}")
			receiveChannel.cancel()
			sendChannel.flushAndClose()
			socket.close()
			userId?.let { server.logout(it) }
		}
	}

	private fun handleRequest(req: SocketRequest) {
		when (req.type) {
			SocketRequestType.LOGIN -> handleLogin(req.payload)
			SocketRequestType.REGISTER -> handleRegister(req.payload)
			SocketRequestType.LOGOUT -> handleLogout()
			SocketRequestType.GET -> handleGet(req.payload)
			SocketRequestType.CREATE -> handleCreate(req.payload)
			SocketRequestType.DELETE -> handleDelete(req.payload)
			SocketRequestType.UPDATE -> handleUpdate(req.payload)
			SocketRequestType.SEARCH -> handleSearch(req.payload)
			SocketRequestType.DOWNLOAD -> handleDownload(req.payload)
			SocketRequestType.MOVE -> handleMove(req.payload)
			SocketRequestType.COPY -> handleCopy(req.payload)
		}
	}

	private fun handleMove(payload: String) {
		if (userId == null) {
			send(SocketResponse(ResponseStatus.ERROR, "Not authenticated"))
			return
		}
		try {
			val req = json.decodeFromString<MoveRequest>(payload)
			val ownerId = req.ownerId
			if (ownerId != userId) {
				send(
					SocketResponse(
						status = ResponseStatus.ERROR,
						message = "Unauthorized access"
					)
				)
				return
			}
			when (req.resource) {
				"folder" -> FolderRepository.moveFolder(req.id, req.targetParentId, ownerId)
				"file" -> FileRepository.moveFile(req.id, req.targetParentId, ownerId)
				else -> {
					send(
						SocketResponse(
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
					return
				}
			}
			send(
				SocketResponse(
					status = ResponseStatus.SUCCESS,
					message = "${req.resource} moved successfully"
				)
			)
		} catch (e: Exception) {
			send(
				SocketResponse(
					status = ResponseStatus.ERROR,
					message = "Move failed: ${e.message}"
				)
			)
		}
	}

	private fun handleCopy(payload: String) {}

	// AUTH HANDLERS
	private fun handleLogin(payload: String) {
		try {
			val dto = json.decodeFromString<LoginRequest>(payload)
			val userOutput = AuthRepository.login(dto.email, dto.password)
			if (userOutput != null) {
				userId = userOutput.id
				server.register(userOutput.id, this)
				send(
					SocketResponse(
						status = ResponseStatus.SUCCESS,
						message = "Login successful",
						data = json.encodeToString(userOutput)
					)
				)
			} else {
				send(
					SocketResponse(
						status = ResponseStatus.ERROR,
						message = "Invalid email or password"
					)
				)
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					status = ResponseStatus.ERROR,
					message = "Login failed: ${e.message}"
				)
			)
		}
	}

	private fun handleRegister(payload: String) {
		try {
			val dto = json.decodeFromString<UserInputDto>(payload)
			val userOutput = AuthRepository.register(dto)
			if (userOutput != null) {
				userId = userOutput.id
				server.register(userOutput.id, this)
				send(
					SocketResponse(
						status = ResponseStatus.SUCCESS,
						message = "Registration successful",
						data = json.encodeToString(userOutput)
					)
				)
			} else {
				send(
					SocketResponse(
						status = ResponseStatus.ERROR,
						message = "Registration failed"
					)
				)
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					status = ResponseStatus.ERROR,
					message = "Registration failed: ${e.message}"
				)
			)
		}
	}

	private fun handleLogout() {
		send(
			SocketResponse(
				status = ResponseStatus.SUCCESS,
				message = "Logout successful"
			)
		)
		disconnect()
	}

	private fun handleGet(payload: String) {
		if (userId == null) {
			send(SocketResponse(ResponseStatus.ERROR, "Not authenticated"))
			return
		}
		try {
			val req = json.decodeFromString<GetRequest>(payload)
			when (req.resource) {
				"users" -> {
					val users = UserRepository.getAll()
					send(
						SocketResponse(
							status = ResponseStatus.SUCCESS,
							message = "Users retrieved",
							data = json.encodeToString(users)
						)
					)
				}

				"user" -> {
					val id = req.id ?: userId
					val user = UserRepository.getById(UUID.fromString(id))
					if (user != null) {
						send(
							SocketResponse(
								status = ResponseStatus.SUCCESS,
								message = "User found",
								data = json.encodeToString(user)
							)
						)
					} else {
						send(
							SocketResponse(
								status = ResponseStatus.ERROR,
								message = "User not found"
							)
						)
					}
				}

				"folder" -> {
					if (req.id == null) {
						send(SocketResponse(ResponseStatus.ERROR, "Folder ID required"))
						return
					}
					val ownerId = req.ownerId ?: userId!!
					val folder = FolderRepository.getFolderById(req.id!!, ownerId)
					val response = GetFolderContentsRequestDto(
						folderId = req.id!!,
						status = if (folder != null) ResponseStatus.SUCCESS else ResponseStatus.ERROR,
						message = if (folder != null) "Folder found" else "Folder not found",
						data = folder
					)
					send(
						SocketResponse(
							status = response.status,
							message = response.message,
							data = json.encodeToString(response)
						)
					)
				}

				"shared-with-me" -> {
					val ownerId = req.ownerId ?: userId!!
					val sharedFolders = FolderRepository.getFoldersSharedWithUser(ownerId)
					val sharedFiles = FileRepository.getFilesSharedWithUser(ownerId)
					val response = GetSharedDataRequest(
						status = if (sharedFolders.isNotEmpty() || sharedFiles.isNotEmpty()) ResponseStatus.SUCCESS else ResponseStatus.ERROR,
						message = if (sharedFolders.isNotEmpty() || sharedFiles.isNotEmpty()) "Shared data retrieved" else "No shared data found",
						folders = sharedFolders,
						files = sharedFiles
					)
					send(
						SocketResponse(
							status = ResponseStatus.SUCCESS,
							message = "Shared folders retrieved",
							data = json.encodeToString(response)
						)
					)
				}

				else -> {
					send(
						SocketResponse(
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
				}
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					status = ResponseStatus.ERROR,
					message = "Get failed: ${e.message}"
				)
			)
		}
	}

	private fun handleSearch(payload: String) {
		if (userId == null) {
			send(SocketResponse(ResponseStatus.ERROR, "Not authenticated"))
			return
		}
		try {
			val req = json.decodeFromString<SearchRequest>(payload)
			when (req.resource) {
				"users" -> {
					val filteredUsers = UserRepository.findByEmailContaining(req.query)
					send(
						SocketResponse(
							status = ResponseStatus.SUCCESS,
							message = "Search completed",
							data = json.encodeToString(filteredUsers)
						)
					)
				}

				else -> {
					send(
						SocketResponse(
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
				}
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					status = ResponseStatus.ERROR,
					message = "Search failed: ${e.message}"
				)
			)
		}
	}

	private fun handleCreate(payload: String) {
		if (userId == null) {
			send(SocketResponse(ResponseStatus.ERROR, "Not authenticated"))
			return
		}
		try {
			val req = json.decodeFromString<CreateRequest>(payload)

			when (req.resource) {
				"user" -> {
					val userDto = json.decodeFromString<UserInputDto>(req.data)
					UserRepository.createUser(userDto)
					send(
						SocketResponse(
							status = ResponseStatus.SUCCESS,
							message = "User created"
						)
					)
				}

				"folder" -> {
					val folderReq = json.decodeFromString<CreateFolderRequest>(req.data)
					val folder = FolderRepository.createFolder(
						folderReq.ownerId,
						folderReq.folderName,
						folderReq.parentFolderId
					)
					val response = CreateFolderResponseDto(
						folder = folder,
						status = if (folder != null) ResponseStatus.SUCCESS else ResponseStatus.ERROR,
						message = if (folder != null) "Folder created" else "Failed to create folder"
					)
					send(
						SocketResponse(
							status = response.status,
							message = response.message,
							data = json.encodeToString(response)
						)
					)
				}

				"file" -> {
					val fileReq = json.decodeFromString<CreateFileRequest>(req.data)
					val encPath =
						FileEncryptor.saveEncryptedFile(
							fileReq.data,
							fileReq.fileName,
							fileReq.ownerId,
							KeyManager.getFixedSecretKeyFromEnv()
						)
					val file = FileRepository.createFile(
						fileName = fileReq.fileName,
						parentFolderId = fileReq.parentFolderId,
						ownerId = fileReq.ownerId,
						fileSize = fileReq.fileSize,
						mimeType = fileReq.mimeType,
						shareIds = fileReq.shareIds,
						storagePath = "${fileReq.ownerId}/$encPath"
					)
					val response = CreateFileResponseDto(
						file = file,
						status = if (file != null) ResponseStatus.SUCCESS else ResponseStatus.ERROR,
						message = if (file != null) "File created" else "Failed to create file"
					)

					send(
						SocketResponse(
							status = response.status,
							message = response.message,
							data = json.encodeToString(response)
						)
					)
				}

				else -> {
					send(
						SocketResponse(
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
				}
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					status = ResponseStatus.ERROR,
					message = "Create failed: ${e.message}"
				)
			)
		}
	}

	private fun handleDelete(payload: String) {
		if (userId == null) {
			send(SocketResponse(ResponseStatus.ERROR, "Not authenticated"))
			return
		}

		try {
			val req = json.decodeFromString<DeleteRequest>(payload)
			val ownerId = req.ownerId ?: userId!!
			val success = when (req.resource) {
				"folder" -> FolderRepository.deleteFolder(req.id, ownerId)
				"file" -> {
					val path = FileRepository.deleteFile(req.id, ownerId)
					val storageDir = File(System.getProperty("user.dir"), "storage").apply { if (!exists()) mkdirs() }
					val file = File(storageDir, path)
					file.delete()
				}

				else -> {
					send(
						SocketResponse(
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
					return
				}
			}

			if (success) {
				send(
					SocketResponse(
						status = ResponseStatus.SUCCESS,
						message = "${req.resource.capitalize()} deleted successfully"
					)
				)
			} else {
				send(
					SocketResponse(
						status = ResponseStatus.ERROR,
						message = "Failed to delete ${req.resource}"
					)
				)
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					status = ResponseStatus.ERROR,
					message = "Delete failed: ${e.message}"
				)
			)
		}
	}

	private fun handleUpdate(payload: String) {
		send(
			SocketResponse(
				status = ResponseStatus.SUCCESS,
				message = "Update not implemented yet"
			)
		)
	}

	private fun handleDownload(payload: String) {
		if (userId == null) {
			send(SocketResponse(ResponseStatus.ERROR, "Not authenticated"))
			return
		}
		try {
			val req = json.decodeFromString<DownloadRequest>(payload)
			println("Download request: $req")
			val ownerId = req.ownerId
			if (ownerId != userId) {
				send(
					SocketResponse(
						status = ResponseStatus.ERROR,
						message = "Unauthorized access"
					)
				)
				return
			}

			when (req.resource) {
				"file" -> {
					val fileRecord = FileRepository.getFileById(req.id, req.ownerId)
					assert(fileRecord != null)
					val bytes = FileDecryptor.loadAndDecryptFile(fileRecord!!.storagePath, KeyManager.getFixedSecretKeyFromEnv())
					if (bytes.isEmpty()) {
						send(
							SocketResponse(
								status = ResponseStatus.ERROR,
								message = "File not found or empty"
							)
						)
						return
					}
					val resource = DownloadFileResource(fileRecord.name, fileRecord.ownerId, fileRecord.mimeType, bytes)
					send(
						SocketResponse(
							status = ResponseStatus.SUCCESS,
							message = "File download ready",
							data = json.encodeToString(resource)
						)
					)
				}

				else -> {
					send(
						SocketResponse(
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
				}
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					status = ResponseStatus.ERROR,
					message = "Download failed: ${e.message}"
				)
			)
		}

	}

	fun send(response: SocketResponse) = scope.launch {
		try {
			val text = json.encodeToString(response)
			sendChannel.writeStringUtf8(text + "\n")
		} catch (e: Exception) {
			println("Error sending response: ${e.message}")
		}
	}

	fun disconnect() {
		try {
			userId?.let { server.logout(it) }
			receiveChannel.cancel()
			sendChannel.cancel(IOException())
			socket.close()
			scope.cancel()
			println("Client disconnected")
		} catch (e: Exception) {
			println("Error disconnecting: ${e.message}")
		}
	}
}
