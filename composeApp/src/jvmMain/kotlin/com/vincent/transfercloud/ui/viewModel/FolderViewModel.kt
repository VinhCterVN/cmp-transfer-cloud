package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.APP_URL
import com.vincent.transfercloud.core.constant.client
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.core.service.CloudinaryService
import com.vincent.transfercloud.data.dto.*
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class FolderViewModel(
	val appState: AppState,
) : ViewModel() {
	private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
	val uiState = _uiState.asStateFlow()
	val folderData = MutableStateFlow<FolderWithContentsDto?>(null)
	val currentUser = appState.currentUser.value!!

	suspend fun getFolderData(folderId: String): String {
		_uiState.value = UIState.Loading
		try {
			delay(50)
			val folder = client
				.get("$APP_URL/folders/$folderId/${currentUser.id}")
				.bodyAsText()
			val res = json.decodeFromString<GetFolderContentsRequestDto>(folder)
			if (res.status == ResponseStatus.ERROR) {
				_uiState.value = UIState.Error(res.message)
				return res.message
			}
			appState.breadcrumb.value = res.data?.folder?.breadcrumb ?: emptyList()
			folderData.value = res.data
			_uiState.value = UIState.Ready
		} catch (e: Exception) {
			e.printStackTrace()
			_uiState.value = UIState.Error(e.message ?: "Unspecified error")
			return e.message ?: "Unspecified error"
		}
		return ""
	}

	suspend fun createFolder(folderName: String, parentId: String): String {
		appState.isCreatingFolder.emit(false)
		if (folderName.trim().isEmpty()) {
			return "Folder name cannot be empty"
		}
		val req = CreateFolderRequestDto(
			currentUser.id,
			folderName,
			parentId
		)

		try {
			val res = client.post("$APP_URL/folders") {
				contentType(ContentType.Application.Json)
				setBody(req)
			}

			if (res.status.value in 200..299) {
				val newFolder: CreateFolderResponseDto = json.decodeFromString<CreateFolderResponseDto>(res.bodyAsText())
				folderData.value = folderData.value?.copy(
					subfolders = folderData.value?.subfolders!!.plus(newFolder.folder!!)
				)
				return newFolder.message
			} else {
				println("Failed to create folder: ${res.status}")
			}
		} catch (e: Exception) {
			println("Error creating folder: ${e.message}")
		}
		return "Failed to create folder"
	}

	suspend fun deleteFolder(folderId: String, ownerId: String): Boolean {
		try {
			val res = client.delete("$APP_URL/folders/$folderId/$ownerId")
			if (res.status.value in 200..299) {
				println("Folder deleted successfully")
				folderData.value?.let { data ->
					folderData.value = data.copy(
						subfolders = data.subfolders.filterNot { it.id == folderId }
					)
				}
				return true
			} else {
				println("Failed to delete folder: ${res.status}")
			}
		} catch (e: Exception) {
			println("Error deleting folder: ${e.message}")
		}
		return false
	}

	suspend fun deleteFile(fileId: String, ownerId: String): Boolean {
		try {
			val res = client.delete("$APP_URL/files/$fileId/$ownerId")
			if (res.status.value in 200..299) {
				println("File deleted successfully")
				folderData.value?.let { data ->
					folderData.value = data.copy(
						files = data.files.filterNot { it.id == fileId }
					)
				}
				return true
			} else {
				println("Failed to delete file: ${res.status}")
			}
		} catch (e: Exception) {
			println("Error deleting file: ${e.message}")
		}
		return false
	}

	fun uploadFile(file: File) = viewModelScope.launch {
		println("Uploading file: $file")
		CloudinaryService.uploadFile(file)
	}
}