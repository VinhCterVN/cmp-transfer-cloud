package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.data.dto.GetRequest
import com.vincent.transfercloud.data.dto.GetSharedDataRequest
import com.vincent.transfercloud.data.dto.SocketRequestType
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShareViewModel(
	appState: AppState,
) : BaseSocketViewModel(appState) {
	private val _uiState = MutableStateFlow<UIState>(UIState.Ready)
	val uiState = _uiState.asStateFlow()

	fun getSharedData() = viewModelScope.launch {
		_uiState.emit(UIState.Loading)
		sendRequest(
			type = SocketRequestType.GET,
			payload = GetRequest(
				resource = "shared-with-me",
				ownerId = appState.currentUser.value!!.id,
			),
			onSuccess = { res ->
				val data = json.decodeFromString<GetSharedDataRequest>(res.data!!)
				println(data.message)
			},
			onError = { e ->
				_uiState.emit(UIState.Error(e))
				println("Error fetching shared data: $e")
			}
		).join()
		_uiState.emit(UIState.Ready)
	}
}