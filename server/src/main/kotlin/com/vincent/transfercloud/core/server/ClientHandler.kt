package com.vincent.transfercloud.core.server

import com.vincent.transfercloud.data.dto.*
import com.vincent.transfercloud.data.repository.AuthRepository
import com.vincent.transfercloud.data.repository.FileRepository
import com.vincent.transfercloud.data.repository.FolderRepository
import com.vincent.transfercloud.data.repository.UserRepository
import com.vincent.transfercloud.utils.json
import io.ktor.network.sockets.*
import io.ktor.util.network.*
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

	init {
		println("ClientHandler initialized for ${socket.remoteAddress}")
		ClientDownloadHandler.init()
	}

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

	private suspend fun handleRequest(req: SocketRequest) {
		when (req.type) {
			SocketRequestType.LOGIN -> handleLogin(req)
			SocketRequestType.REGISTER -> handleRegister(req)
			SocketRequestType.LOGOUT -> handleLogout()
			SocketRequestType.GET -> handleGet(req)
			SocketRequestType.CREATE -> handleCreate(req)
			SocketRequestType.DELETE -> handleDelete(req)
			SocketRequestType.UPDATE -> handleUpdate(req)
			SocketRequestType.SEARCH -> handleSearch(req)
			SocketRequestType.DOWNLOAD -> handleDownload(req)
			SocketRequestType.MOVE -> handleMove(req)
			SocketRequestType.COPY -> handleCopy(req)
			SocketRequestType.SHARE -> handleShare(req)
		}
	}

	private fun handleMove(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		if (userId == null) {
			send(SocketResponse(requestId, ResponseStatus.ERROR, "Not authenticated"))
			return
		}
		try {
			val req = json.decodeFromString<MoveRequest>(payload)
			val ownerId = req.ownerId
			if (ownerId != userId) {
				send(
					SocketResponse(
						id = requestId,
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
							id = requestId,
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
					id = requestId,
					status = ResponseStatus.ERROR,
					message = "Move failed: ${e.message}"
				)
			)
		}
	}

	private fun handleCopy(req: SocketRequest) {}

	private fun handleShare(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		try {
			val req = json.decodeFromString<ShareRequest>(payload)
			when (req.resource) {
				"file" -> {
					FileRepository.shareFile(req.resourceId, req.ownerId, req.shareToEmail, req.permission)
				}

				"folder" -> {
					FolderRepository.shareFolder(req.resourceId, req.ownerId, req.shareToEmail, req.permission)
				}

				else -> {
					send(
						SocketResponse(
							id = requestId,
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
					message = "${req.resource} shared successfully"
				)
			)
		} catch (e: Exception) {
			send(
				SocketResponse(
					id = requestId,
					status = ResponseStatus.ERROR,
					message = "Share failed: ${e.message}"
				)
			)
		}
	}

	private fun handleLogin(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		try {
			val dto = json.decodeFromString<LoginRequest>(payload)
			val userOutput = AuthRepository.login(dto.email, dto.password)
			if (userOutput != null) {
				userId = userOutput.id
				server.register(userOutput.id, this)
				send(
					SocketResponse(
						id = requestId,
						status = ResponseStatus.SUCCESS,
						message = "Login successful",
						data = json.encodeToString(userOutput)
					)
				)
			} else {
				send(
					SocketResponse(
						id = requestId,
						status = ResponseStatus.ERROR,
						message = "Invalid email or password"
					)
				)
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					id = requestId,
					status = ResponseStatus.ERROR,
					message = "Login failed: ${e.message}"
				)
			)
		}
	}

	private fun handleRegister(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		try {
			val dto = json.decodeFromString<UserInputDto>(payload)
			val userOutput = AuthRepository.register(dto)
			if (userOutput != null) {
				userId = userOutput.id
				server.register(userOutput.id, this)
				send(
					SocketResponse(
						id = requestId,
						status = ResponseStatus.SUCCESS,
						message = "Registration successful",
						data = json.encodeToString(userOutput)
					)
				)
			} else {
				send(
					SocketResponse(
						id = requestId,
						status = ResponseStatus.ERROR,
						message = "Registration failed"
					)
				)
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					id = requestId,
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

	private suspend fun handleGet(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		if (userId == null) {
			send(SocketResponse(requestId, ResponseStatus.ERROR, "Not authenticated"))
			return
		}
		try {
			val req = json.decodeFromString<GetRequest>(payload)
			when (req.resource) {
				"users" -> {
					val users = UserRepository.getAll()
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.SUCCESS,
							message = "Users retrieved",
							data = json.encodeToString(users)
						)
					)
				}

				"user" -> {
					val reqId = req.id ?: userId
					val user = UserRepository.getById(UUID.fromString(reqId))
					if (user != null) {
						send(
							SocketResponse(
								id = requestId,
								status = ResponseStatus.SUCCESS,
								message = "User found",
								data = json.encodeToString(user)
							)
						)
					} else {
						send(
							SocketResponse(
								id = requestId,
								status = ResponseStatus.ERROR,
								message = "User not found"
							)
						)
					}
				}

				"folder" -> {
					if (req.id == null) {
						send(SocketResponse(requestId, ResponseStatus.ERROR, "Folder ID required"))
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
					send(SocketResponse(requestId, response.status, response.message, json.encodeToString(response)))
				}

				"shared-with-me" -> {
					val ownerId = req.ownerId ?: userId!!
					val sharedFolders = FolderRepository.getFoldersSharedWithUser(ownerId)
					val sharedFiles = FileRepository.getFilesSharedWithUser(ownerId)
					val response = GetSharedDataResponse(
						status = if (sharedFolders.isNotEmpty() || sharedFiles.isNotEmpty()) ResponseStatus.SUCCESS else ResponseStatus.ERROR,
						message = if (sharedFolders.isNotEmpty() || sharedFiles.isNotEmpty()) "Shared data retrieved" else "No shared data found",
						folders = sharedFolders,
						files = sharedFiles
					)
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.SUCCESS,
							message = "Shared folders retrieved",
							data = json.encodeToString(response)
						)
					)
				}

				"file-shared-info" -> {
					if (req.id == null) {
						send(SocketResponse(requestId, ResponseStatus.ERROR, "File ID required"))
						return
					}
					val shares = FileRepository.getFileSharedInfo(req.id!!, req.ownerId!!)
					val response = FileSharesInfoDto(
						fileId = req.id!!,
						ownerId = req.ownerId!!,
						shares = shares
					)
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.SUCCESS,
							message = "File shared info retrieved",
							data = json.encodeToString(response)
						)
					)
				}

				"folder-shared-info" -> {
					if (req.id == null) {
						send(SocketResponse(requestId, ResponseStatus.ERROR, "Folder ID required"))
						return
					}
					val shares = FolderRepository.getFolderSharedInfo(req.id!!, req.ownerId!!)
					val response = FolderSharesInfoDto(
						folderId = req.id!!,
						ownerId = req.ownerId!!,
						shares = shares
					)
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.SUCCESS,
							message = "Folder shared info retrieved",
							data = json.encodeToString(response)
						)
					)
				}

				"file-thumbnail" -> {
					if (req.id == null) {
						send(SocketResponse(requestId, ResponseStatus.ERROR, "File ID required"))
						return
					}
					val fileRecord = FileRepository.getFileById(req.id!!, req.ownerId!!)
					val bytes = createFileThumbnailBytes(fileRecord!!.name, fileRecord.ownerId, null, fileRecord.storagePath)
					val response = GetThumbnailResponseDto(
						fileId = req.id!!,
						status = if (bytes != null) ResponseStatus.SUCCESS else ResponseStatus.ERROR,
						message = if (bytes != null) "Thumbnail retrieved" else "Thumbnail not found",
						thumbnailBytes = bytes
					)
					send(SocketResponse(requestId, response.status, response.message, json.encodeToString(response)))
				}

				"file-summarize" -> {
					val fileRecord = FileRepository.getFileById(req.id!!, req.ownerId!!)
					assert(fileRecord != null)
					val response = FileSummarizer.request(fileRecord)
					send(SocketResponse(id = requestId, status = ResponseStatus.SUCCESS, message = "File summarized", data = response))
				}

				else -> {
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
				}
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					id = requestId,
					status = ResponseStatus.ERROR,
					message = "Get failed: ${e.message}"
				)
			)
		}
	}

	private fun handleSearch(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		if (userId == null) {
			send(SocketResponse(requestId, ResponseStatus.ERROR, "Not authenticated"))
			return
		}
		try {
			val req = json.decodeFromString<SearchRequest>(payload)
			when (req.resource) {
				"users" -> {
					val filteredUsers = UserRepository.findByEmailContaining(req.query)
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.SUCCESS,
							message = "Search completed",
							data = json.encodeToString(filteredUsers)
						)
					)
				}

				else -> {
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
				}
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					id = requestId,
					status = ResponseStatus.ERROR,
					message = "Search failed: ${e.message}"
				)
			)
		}
	}

	private fun handleCreate(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		if (userId == null) {
			send(SocketResponse(requestId, ResponseStatus.ERROR, "Not authenticated"))
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
							id = requestId,
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
							id = requestId,
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

					send(SocketResponse(requestId, response.status, response.message, json.encodeToString(response)))
				}

				else -> {
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
				}
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					id = requestId,
					status = ResponseStatus.ERROR,
					message = "Create failed: ${e.message}"
				)
			)
		}
	}

	private fun handleDelete(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		if (userId == null) {
			send(SocketResponse(requestId, ResponseStatus.ERROR, "Not authenticated"))
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
							id = requestId,
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
					return
				}
			}

			if (success) send(SocketResponse(requestId, ResponseStatus.SUCCESS, "${req.resource.capitalize()} deleted successfully"))
			else send(SocketResponse(requestId, ResponseStatus.ERROR, "Failed to delete ${req.resource}"))

		} catch (e: Exception) {
			send(SocketResponse(requestId, ResponseStatus.ERROR, "Delete failed: ${e.message}"))
		}
	}

	private fun handleUpdate(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		try {
			val req = json.decodeFromString<UpdateRequest>(payload)
			when (req.resource) {
				"file" -> {
					val record = FileRepository.getFileById(req.id, req.ownerId)
					assert(record != null) {
						"File with ID ${req.id} not found for owner ${req.ownerId}."
					}
					if (FileRepository.renameFile(req.id, req.data, req.ownerId)) {
						send(
							SocketResponse(
								id = requestId,
								status = ResponseStatus.SUCCESS,
								message = "File renamed successfully",
								data = json.encodeToString(
									RenameFolderResponseDto(
										folder = null,
										status = ResponseStatus.SUCCESS,
										message = "File renamed successfully"
									)
								)
							)
						)
					} else {
						send(
							SocketResponse(
								id = requestId,
								status = ResponseStatus.ERROR,
								message = "Failed to rename file"
							)
						)
					}
				}

				"folder" -> {
					val record = FolderRepository.getFolderById(req.id, req.ownerId)
					assert(record != null) {
						"Folder with ID ${req.id} not found for owner ${req.ownerId}."
					}
					if (FolderRepository.renameFolder(req.id, req.data, req.ownerId)) {
						send(
							SocketResponse(
								id = requestId,
								status = ResponseStatus.SUCCESS,
								message = "Folder renamed successfully",
								data = json.encodeToString(
									RenameFolderResponseDto(
										record!!.folder,
										ResponseStatus.SUCCESS,
										"Folder renamed successfully"
									)
								)
							)
						)
					} else {
						send(
							SocketResponse(
								id = requestId,
								status = ResponseStatus.ERROR,
								message = "Failed to rename folder"
							)
						)
					}
				}
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					id = requestId,
					status = ResponseStatus.ERROR,
					message = "Update failed: ${e.message}"
				)
			)
		}
	}

	private fun handleDownload(request: SocketRequest) {
		val requestId = request.id
		val payload = request.payload
		if (userId == null) {
			send(SocketResponse(requestId, ResponseStatus.ERROR, "Not authenticated"))
			return
		}
		try {
			val req = json.decodeFromString<DownloadRequest>(payload)
			println("Download request: $req")

			when (req.resource) {
				"file" -> {
					val fileRecord = FileRepository.getFileById(req.id, req.ownerId)
					assert(fileRecord != null)
					val bytes = FileDecryptor.loadAndDecryptFile(fileRecord!!.storagePath, KeyManager.getFixedSecretKeyFromEnv())
					if (bytes.isEmpty()) {
						send(
							SocketResponse(
								id = requestId,
								status = ResponseStatus.ERROR,
								message = "File not found or empty"
							)
						)
						return
					}
					val resource = DownloadFileResource(fileRecord.name, fileRecord.ownerId, fileRecord.mimeType, bytes)
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.SUCCESS,
							message = "File download ready",
							data = json.encodeToString(resource)
						)
					)
				}

				"folder" -> {
					val folderRecord = FolderRepository.getFolderById(req.id, req.ownerId)
					assert(folderRecord != null) { "Folder with ID ${req.id} not found." }
					val address = ClientDownloadHandler.getAddress()
					if (address == null) ClientDownloadHandler.initSocket()
					val payload = FolderDownloadMetadata(
						"READY",
						folderRecord!!.folder.name,
						address!!.toJavaAddress().hostname,
						address.toJavaAddress().port
					)
					send(SocketResponse(requestId, ResponseStatus.SUCCESS, "Folder download ready", json.encodeToString(payload)))
					ClientDownloadHandler.handleClientDownload(folderRecord.folder.id)
				}

				else -> {
					send(
						SocketResponse(
							id = requestId,
							status = ResponseStatus.ERROR,
							message = "Unknown resource: ${req.resource}"
						)
					)
				}
			}
		} catch (e: Exception) {
			send(
				SocketResponse(
					id = requestId,
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
