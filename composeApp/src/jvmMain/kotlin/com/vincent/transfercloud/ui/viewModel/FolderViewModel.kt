package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.data.dto.*
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class FolderViewModel(
	appState: AppState,
) : BaseSocketViewModel(appState) {
	private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
	val uiState = _uiState.asStateFlow()
	val folderData = MutableStateFlow<FolderWithContentsDto?>(null)
	val currentUser = appState.currentUser.value!!
	val filteredUsers = MutableStateFlow<List<UserOutputDto>>(emptyList())

	suspend fun getFolderData(folderId: String) {
		_uiState.value = UIState.Loading
		delay(50)
		sendRequest(
			type = SocketRequestType.GET,
			payload = GetRequest("folder", folderId, appState.currentUser.value!!.id),
			onSuccess = { res ->
				val data = json.decodeFromString<GetFolderContentsRequestDto>(res.data!!)
				appState.breadcrumb.value = data.data?.folder?.breadcrumb ?: emptyList()
				folderData.value = data.data
				_uiState.value = UIState.Ready
			},
			onError = { msg ->
				_uiState.value = UIState.Error(msg)
			}
		)
	}

	suspend fun createFolder(folderName: String, parentId: String): String {
		appState.isCreatingFolder.emit(false)
		if (folderName.trim().isEmpty()) {
			return "Folder name cannot be empty"
		}
		var fnRes = "Failed to create folder"
		sendRequest(
			type = SocketRequestType.CREATE,
			payload = CreateRequest(
				"folder", json.encodeToString(
					CreateFolderRequest(
						currentUser.id,
						folderName,
						parentId
					)
				)
			),
			onSuccess = { res ->
				val newFolder: CreateFolderResponseDto = json.decodeFromString<CreateFolderResponseDto>(res.data!!)
				folderData.value = folderData.value?.copy(
					subfolders = folderData.value?.subfolders!!.plus(newFolder.folder!!)
				)
				fnRes = newFolder.message
			},
			onError = { str ->
				println("Error creating folder: $str")
			}
		).join()
		return fnRes
	}

	fun deleteFolder(folderId: String, ownerId: String): Boolean {
		var status = true
		sendRequest(
			type = SocketRequestType.DELETE,
			payload = DeleteRequest("folder", folderId, ownerId),
			onSuccess = { res ->
				println("Folder deleted successfully")
				folderData.value?.let { data ->
					folderData.value = data.copy(
						subfolders = data.subfolders.filterNot { it.id == folderId }
					)
				}
			},
			onError = { msg ->
				println("Error deleting folder: $msg")
				status = false
			}
		)
		return status
	}

	fun deleteFile(fileId: String, ownerId: String): Boolean {
		var status = true
		sendRequest(
			type = SocketRequestType.DELETE,
			payload = DeleteRequest("file", fileId, ownerId),
			onSuccess = { res ->
				println("File deleted successfully")
				folderData.value?.let { data ->
					folderData.value = data.copy(
						files = data.files.filterNot { it.id == fileId }
					)
				}
			},
			onError = { msg ->
				println("Error deleting folder: $msg")
				status = false
			}
		)
		return status
	}

	fun searchUsersByEmail(query: String) {
		sendRequest(
			type = SocketRequestType.SEARCH,
			payload = SearchRequest("users", "email", query),
			onSuccess = { res ->
				val data = json.decodeFromString<List<UserOutputDto>>(res.data!!)
				filteredUsers.value = data
			},
			onError = { msg ->
				println("Error searching users: $msg")
			}
		)
	}

	fun uploadFile(file: File?) = viewModelScope.launch {
		if (file == null) return@launch
		println("Uploading file: $file")
		val req = CreateFileRequest(
			ownerId = currentUser.id,
			fileName = file.name,
			parentFolderId = appState.currentFolder.value,
			fileSize = file.length(),
			mimeType = file.getMimeType(),
			data = file.readBytes()
		)

		sendRequest(
			type = SocketRequestType.CREATE,
			payload = CreateRequest(
				"file",
				json.encodeToString(req)
			),
			onSuccess = { res ->
				val newFile: CreateFileResponseDto = json.decodeFromString<CreateFileResponseDto>(res.data!!)
				folderData.value = folderData.value?.copy(
					files = folderData.value?.files!!.plus(newFile.file!!)
				)
				println("File uploaded successfully: ${newFile.message}")
			},
			onError = { msg ->
				println("Error uploading file: $msg")
			}
		)
	}
}

fun File.getMimeType(): String {
	return when (extension.lowercase()) {
		"jpg", "jpeg" -> "image/jpeg"
		"png" -> "image/png"
		"gif" -> "image/gif"
		"mp4" -> "video/mp4"
		"mp3" -> "audio/mpeg"
		"pdf" -> "application/pdf"
		"doc", "docx" -> "application/msword"
		"xls", "xlsx" -> "application/vnd.ms-excel"
		"ppt", "pptx" -> "application/vnd.ms-powerpoint"
		"txt" -> "text/plain"
		else -> "application/octet-stream"
	}
}