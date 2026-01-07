package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.core.server.SocketRepository
import com.vincent.transfercloud.data.dto.GetRequest
import com.vincent.transfercloud.data.dto.SocketRequestType
import com.vincent.transfercloud.data.dto.SummarizeResponseDto
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FileDetailVM(
	appState: AppState,
	socketRepository: SocketRepository
) : BaseSocketViewModel(appState, socketRepository) {
	private val _uiState = MutableStateFlow<UIState>(UIState.Ready)
	val uiState = _uiState.asStateFlow()
	private val _summarizeResponse = MutableStateFlow("")
	val summarizeResponse = _summarizeResponse.asStateFlow()

	init {
		println("FileDetailViewModel created")
	}

	fun requestSummarization(id: String, ownerId: String) = viewModelScope.launch(Dispatchers.IO) {
		_uiState.emit(UIState.Loading)
		sendRequest(
			type = SocketRequestType.GET,
			payload = GetRequest(
				id = id,
				ownerId = ownerId,
				resource = "file-summarize"
			),
			onSuccess = { res ->
				val data = json.decodeFromString<SummarizeResponseDto>(res.data!!)
				_summarizeResponse.value = data.data
			},
			onError = { e ->
				println(e); _uiState.emit(UIState.Error(e))
			}
		).join()
		_uiState.emit(UIState.Ready)
	}

	override fun onCleared() {
		super.onCleared()
		println("FileDetailViewModel cleared")
	}
}