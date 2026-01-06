package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.core.server.SocketRepository
import com.vincent.transfercloud.data.dto.ResponseStatus
import com.vincent.transfercloud.data.dto.SocketRequest
import com.vincent.transfercloud.data.dto.SocketRequestType
import com.vincent.transfercloud.data.dto.SocketResponse
import com.vincent.transfercloud.ui.state.AppState
import io.ktor.network.selector.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

open class BaseSocketViewModel(
	protected val appState: AppState,
	protected val socketRepository: SocketRepository
) : ViewModel() {
	protected val selectorManager: SelectorManager = ActorSelectorManager(Dispatchers.IO)
	protected val sendChannel get() = appState.clientSocketWriteChannel
	protected val receiveChannel get() = appState.clientSocketReadChannel
	protected val pendingRequests = ConcurrentHashMap<String, PendingRequest>()

	init {
		viewModelScope.launch {
			socketRepository.socketResponseFlow.distinctUntilChanged().collect { res ->
				val requestId = res.id
				val pending = pendingRequests.remove(requestId)
				if (pending != null) {
					if (res.status == ResponseStatus.SUCCESS) {
						pending.onSuccess(res)
					} else {
						pending.onError(res.message)
					}
				}
			}
		}
	}

	protected inline fun <reified T : @Serializable Any> sendRequest(
		type: SocketRequestType,
		payload: T,
		noinline onSuccess: suspend (SocketResponse) -> Unit,
		noinline onError: suspend (String) -> Unit
	) = viewModelScope.launch(Dispatchers.IO) {
		try {
			val jsonPayload = json.encodeToString(payload)
			val req = SocketRequest(
				type = type,
				payload = jsonPayload
			)
			pendingRequests[req.id] = PendingRequest(onSuccess, onError)
			send(req)
		} catch (e: Exception) {
			onError("Socket error: ${e.message} - ${e.javaClass.name}")
		}
	}

	protected inline fun <reified T : @Serializable Any> send(data: T) = viewModelScope.launch {
		val req = json.encodeToString(data) + "\n"
		socketRepository.writeToSocket(req)
//		sendChannel.value?.writeStringUtf8(req)
	}

	protected data class PendingRequest(
		val onSuccess: suspend (SocketResponse) -> Unit,
		val onError: suspend (String) -> Unit
	)
}
