package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.ViewModel
import com.vincent.transfercloud.SERVER_URL
import com.vincent.transfercloud.core.constant.client
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.data.dto.CreateFolderRequestDto
import com.vincent.transfercloud.data.dto.CreateFolderResponseDto
import com.vincent.transfercloud.data.dto.FolderWithContentsDto
import com.vincent.transfercloud.data.dto.GetFolderContentsRequestDto
import com.vincent.transfercloud.data.dto.ResponseStatus
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FolderViewModel(
	val appState: AppState,
) : ViewModel() {
	private val _uiState = MutableStateFlow<UIState>(UIState.Ready)
	val uiState = _uiState.asStateFlow()
	val folderData = MutableStateFlow<FolderWithContentsDto?>(null)
	val currentUser = appState.currentUser.value!!

	suspend fun getFolderData(folderId: String): String {
		_uiState.value = UIState.Loading
		try {
			delay(500)
			val folder = client
				.get("$SERVER_URL/folders/$folderId/${currentUser.id}")
				.bodyAsText()
			println(folder)
			val res = json.decodeFromString<GetFolderContentsRequestDto>(folder)
			if (res.status == ResponseStatus.ERROR) {
				_uiState.value = UIState.Error(res.message)
				return res.message
			}
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
		val req = CreateFolderRequestDto(
			currentUser.id,
			folderName,
			parentId
		)

		try {
			val res = client.post("$SERVER_URL/folders") {
				contentType(ContentType.Application.Json)
				setBody(req)
			}

			if (res.status.value in 200..299) {
				val newFolder: CreateFolderResponseDto = json.decodeFromString<CreateFolderResponseDto>(res.bodyAsText())
				appState.isCreatingFolder.emit(false)
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
}