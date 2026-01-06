package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.core.server.SocketRepository
import com.vincent.transfercloud.data.dto.*
import com.vincent.transfercloud.data.enum.SharePermission
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShareDialogVM(
	appState: AppState, socketRepository: SocketRepository
) : BaseSocketViewModel(appState, socketRepository) {
	val currentUser = appState.currentUser.value!!
	val filteredUsers = MutableStateFlow<List<UserOutputDto>>(emptyList())
	private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
	val uiState = _uiState.asStateFlow()
	private val _sharesInfo = MutableStateFlow<List<ShareMetadata>>(emptyList())
	val sharesInfo = _sharesInfo.asStateFlow()

	init {

	}

	fun setUIState(state: UIState) {
		_uiState.value = state
	}

	fun searchUsersByEmail(query: String) = viewModelScope.launch {
		if (query.isEmpty()) return@launch
		sendRequest(
			type = SocketRequestType.SEARCH,
			payload = SearchRequest("users", "email", query),
			onSuccess = { res ->
				val data = json.decodeFromString<List<UserOutputDto>>(res.data!!)
				filteredUsers.value = data.filter { it.id != currentUser.id }
			},
			onError = { msg ->
				_uiState.value = UIState.Error(msg)
				println("Error searching users: $msg")
			}
		)
	}

	fun getSharesInfo(id: String, ownerId: String, isFolder: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
		if (id.isEmpty()) return@launch
		println("Fetching shares info for id: $id, ownerId: $ownerId, isFolder: $isFolder")
		_uiState.emit(UIState.Loading)
		val resource = if (isFolder) "folder-shared-info" else "file-shared-info"
		sendRequest(
			type = SocketRequestType.GET,
			payload = GetRequest(resource, id, ownerId),
			onSuccess = { res ->
				if (isFolder) {
					val data = json.decodeFromString<FolderSharesInfoDto>(res.data!!)
					_sharesInfo.emit(data.shares)
				} else {
					val data = json.decodeFromString<FileSharesInfoDto>(res.data!!)
					_sharesInfo.emit(data.shares)
				}
				_uiState.emit(UIState.Ready)
			},
			onError = {
				_uiState.emit(UIState.Error(it))
			}
		)
	}

	fun handleShare(sharingItem: Pair<String, Boolean>, ownerId: String, permission: SharePermission, shareToId: String) =
		viewModelScope.launch(Dispatchers.IO) {
			sendRequest(
				type = SocketRequestType.SHARE,
				payload = ShareRequest(
					resourceId = sharingItem.first,
					ownerId = ownerId,
					shareToEmail = shareToId,
					permission = permission,
					resource = if (sharingItem.second) "folder" else "file"
				),
				onSuccess = { res -> println(res)},
				onError = { e ->
					println("Error sharing item: $e")
				}
			)
		}
}