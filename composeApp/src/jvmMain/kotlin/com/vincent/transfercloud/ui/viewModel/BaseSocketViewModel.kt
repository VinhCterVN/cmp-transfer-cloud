package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.data.dto.ResponseStatus
import com.vincent.transfercloud.data.dto.SocketRequest
import com.vincent.transfercloud.data.dto.SocketRequestType
import com.vincent.transfercloud.data.dto.SocketResponse
import com.vincent.transfercloud.ui.state.AppState
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

open class BaseSocketViewModel(
	protected val appState: AppState
) : ViewModel() {
	protected val sendChannel get() = appState.clientSocketWriteChannel
	protected val receiveChannel get() = appState.clientSocketReadChannel

	protected inline fun <reified T : @Serializable Any> sendRequest(
		type: SocketRequestType,
		payload: T,
		crossinline onSuccess: suspend (SocketResponse) -> Unit,
		crossinline onError: suspend (String) -> Unit
	) = viewModelScope.launch {
		try {
			val jsonPayload = json.encodeToString(payload)
			val req = SocketRequest(
				type = type,
				payload = jsonPayload
			)
			send(req)
			val line = receiveChannel.value?.readUTF8Line()

			if (line.isNullOrEmpty()) {
				onError("Empty response from server")
				return@launch
			}
			val res = json.decodeFromString<SocketResponse>(line)

			if (res.status == ResponseStatus.SUCCESS) {
				onSuccess(res)
			} else {
				onError(res.message)
			}

		} catch (e: Exception) {
			onError("Socket error: ${e.message}")
		}
	}

	protected inline fun <reified T : @Serializable Any> send(data: T) = viewModelScope.launch {
		val req = json.encodeToString(data) + "\n"
		sendChannel.value?.writeStringUtf8(req)
	}
}
