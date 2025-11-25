package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.data.dto.*
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.FileViewIndex
import com.vincent.transfercloud.ui.state.UIState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class FolderViewModel(
	appState: AppState,
) : BaseSocketViewModel(appState) {
	val currentUser = appState.currentUser.value!!
	val folderData = MutableStateFlow<FolderWithContentsDto?>(null)
	val filteredUsers = MutableStateFlow<List<UserOutputDto>>(emptyList())
	private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
	val selectedIds = _selectedIds.asStateFlow()
	val currentViewIndex = MutableStateFlow(FileViewIndex.GRID)
	private val _draggedItem = MutableStateFlow<Pair<String, FolderObject>?>(null)
	val draggedItem = _draggedItem.asStateFlow()
	private val _hoveredFolderId = MutableStateFlow<String?>(null)
	val hoveredFolderId = _hoveredFolderId.asStateFlow()
	private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
	val uiState = _uiState.asStateFlow()

	init {
		viewModelScope.launch {
			appState.currentFolder.collect { newId ->
				if (newId.isNotEmpty()) getFolderData(newId)
			}
		}
	}

	fun setSelectedIds(ids: Set<String>) {
		_selectedIds.value = ids
	}

	fun toggleSelection(id: String, isCtrlPressed: Boolean) {
		_selectedIds.value = if (isCtrlPressed) {
			if (_selectedIds.value.contains(id)) _selectedIds.value - id else _selectedIds.value + id
		} else setOf(id)
	}

	fun selectAll() {
		_selectedIds.value = folderData.value?.let { data ->
			(data.subfolders.map { it.id } + data.files.map { it.id }).toSet()
		} ?: emptySet()
	}

	fun startDragging(item: Pair<String, FolderObject>) {
		_draggedItem.value = item
	}

	fun stopDragging() {
		_draggedItem.value = null
	}

	fun setHoveredFolder(folderId: String?) {
		_hoveredFolderId.value = folderId
	}

	fun moveItem(targetFolderId: String) {
		val dragged = draggedItem.value
		sendRequest(
			type = SocketRequestType.MOVE,
			payload = MoveRequest(
				resource = if (dragged!!.second == FolderObject.FOLDER) "folder" else "file",
				id = dragged.first,
				targetParentId = targetFolderId,
				ownerId = currentUser.id
			),
			onSuccess = { res ->
				if (dragged.second == FolderObject.FOLDER) {
					folderData.value = folderData.value?.copy(
						subfolders = folderData.value?.subfolders!!.filterNot { it.id == dragged.first }
					)
				} else {
					folderData.value = folderData.value?.copy(
						files = folderData.value?.files!!.filterNot { it.id == dragged.first }
					)
				}
			},
			onError = { e ->
				println("Error moving item: $e")
			}
		)
		println("Moved to $targetFolderId")
	}

	suspend fun getFolderData(folderId: String = appState.currentFolder.value) {
		if (folderId.isEmpty()) return;
		_uiState.value = UIState.Loading
		delay(50)
		sendRequest(
			type = SocketRequestType.GET,
			payload = GetRequest("folder", folderId, appState.currentUser.value!!.id),
			onSuccess = { res ->
				val data = json.decodeFromString<GetFolderContentsRequestDto>(res.data!!)
				appState.breadcrumb.value = data.data?.folder?.breadcrumb ?: emptyList()
				folderData.value = data.data?.copy(
					subfolders = data.data?.subfolders?.sortedBy { it.name } ?: emptyList(),
				)
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
						folderName.trim(),
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
				filteredUsers.value = data.filter { it.id != currentUser.id }
			},
			onError = { msg ->
				println("Error searching users: $msg")
			}
		)
	}

	fun uploadFile(file: File?, shareIds: List<String> = emptyList()) = viewModelScope.launch {
		if (file == null) return@launch
		println("Uploading file: $file with ${shareIds.size} shares")
		val req = CreateFileRequest(
			ownerId = currentUser.id,
			fileName = file.name,
			parentFolderId = appState.currentFolder.value,
			fileSize = file.length(),
			mimeType = file.getMimeType(),
			shareIds = shareIds,
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

	fun downloadFile(file: FileOutputDto) = viewModelScope.launch {
		sendRequest(
			type = SocketRequestType.DOWNLOAD,
			payload = DownloadRequest(resource = "file", id = file.id, ownerId = file.ownerId),
			onSuccess = { res ->
				val data = json.decodeFromString<DownloadFileResource>(res.data!!)
				val fileSaver = FileKit.openFileSaver(suggestedName = file.name, extension = file.name.substringAfterLast('.', ""))
				fileSaver?.write(data.data)
				println("File downloaded successfully: ${fileSaver?.absolutePath()}")
			},
			onError = { msg ->
				println("Error downloading file: $msg")
			}
		)
	}

	fun downloadFolder(folder: FolderOutputDto) = viewModelScope.launch {
//		TODO: Implement folder download
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

enum class FolderObject {
	FOLDER, FILE
}