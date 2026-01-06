package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.core.model.NetworkConfig
import com.vincent.transfercloud.core.server.SocketRepository
import com.vincent.transfercloud.data.dto.LoginRequest
import com.vincent.transfercloud.data.dto.SocketRequestType
import com.vincent.transfercloud.data.dto.UserInputDto
import com.vincent.transfercloud.data.dto.UserOutputDto
import com.vincent.transfercloud.ui.state.AppState
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.io.IOException
import java.net.ConnectException

class AppViewModel(
	appState: AppState,
	socketRepository: SocketRepository
) : BaseSocketViewModel(appState, socketRepository) {
	private var password: String? = null
	private val socket = appState.clientSocket
	private var reconnectJob: Job? = null
	private var reconnectAttempts = 0
	private val maxReconnectAttempts = 10
	private val baseReconnectDelayMs = 2000L

	init {
		viewModelScope.launch(Dispatchers.IO) {
			appState.networkConfig.collect { newConfig ->
				println("Network Config Updated: $newConfig")
				reconnectWithNewConfig()
			}
		}
	}

	private fun connect() = viewModelScope.launch(Dispatchers.IO) {
		try {
			reconnectJob?.cancel()
			disconnect(false)
			val config = appState.networkConfig.value
			socket.value = aSocket(selectorManager).tcp().connect(config.host, config.port)
			sendChannel.value = socket.value!!.openWriteChannel(autoFlush = true)
			receiveChannel.value = socket.value!!.openReadChannel()

			if (receiveChannel.value != null) {
				println("Connected successfully")
			}

			appState.isConnected.emit(true)
			reconnectAttempts = 0

		} catch (_: ConnectException) {
			handleConnectionLost("Unable to connect to server at ${appState.networkConfig.value.host}:${appState.networkConfig.value.port}")
		} catch (e: Exception) {
			handleConnectionLost("Connection lost: ${e.message}")
		}
	}

	private fun handleConnectionLost(reason: String) {
		println(reason)
		appState.isConnected.value = false
		scheduleReconnect()
	}

	private suspend fun reconnectWithNewConfig() {
		val currentUser = appState.currentUser.value

		disconnect(closeSelector = false)
		connect().join()

		currentUser?.let { user ->
			delay(500)
			login(user.email, password!!)
			println("Auto-login after config change")
		}
	}

	private fun scheduleReconnect() {
		reconnectJob?.cancel()
		if (reconnectAttempts < maxReconnectAttempts) {
			val delayMs = baseReconnectDelayMs * (1 shl reconnectAttempts)
			reconnectAttempts++

			println("Scheduling reconnect attempt $reconnectAttempts in ${delayMs}ms")
			reconnectJob = viewModelScope.launch(Dispatchers.IO) {
				delay(delayMs)
				connect()
			}
		} else {
			println("Max reconnect attempts reached")
		}
	}

	override fun onCleared() {
		super.onCleared()
		sendChannel.value?.cancel(IOException())
		receiveChannel.value?.cancel(IOException())
		socket.value?.close()
		selectorManager.close()
		println("AppViewModel cleared and resources released.")
	}

	fun login(email: String, password: String): String? {
		if (email.isBlank() || password.isBlank()) return "Email and password cannot be empty"
		var res: String? = ""
		sendRequest(
			type = SocketRequestType.LOGIN,
			payload = LoginRequest(email, password),
			onSuccess = { res ->
				val user = json.decodeFromString<UserOutputDto>(res.data!!)
				this.password = password
				appState.currentUser.value = user
			},
			onError = { msg ->
				println("Login failed: $msg")
				res = "Login failed: $msg"
			}
		)
		return res
	}

	fun register(fullName: String, email: String, password: String): String? {
		if (email.isBlank() || password.isBlank()) return "Email and password cannot be empty"
		var res: String? = ""
		sendRequest(
			type = SocketRequestType.REGISTER,
			payload = UserInputDto(fullName, email, password),
			onSuccess = { res ->
				val user = json.decodeFromString<UserOutputDto>(res.data!!)
				appState.currentUser.value = user
			},
			onError = { msg ->
				println("Register failed: $msg")
				res = "Register failed: $msg"
			}
		)
		return res
	}

	fun logout() {
		appState.currentUser.value = null
	}

	private suspend fun disconnect(closeSelector: Boolean = true) = viewModelScope.launch(Dispatchers.IO) {
		try {
			sendChannel.value?.flushAndClose()
			receiveChannel.value?.cancel()
			socket.value?.close()

			println("Disconnected (closeSelector: $closeSelector)")
		} catch (e: Exception) {
			println("Error during disconnect: ${e.message}")
		} finally {
			if (closeSelector) {
				selectorManager.close()
			}
		}
	}.join()

	fun setNetworkConfig(host: String, port: Int) {
		val newConfig = NetworkConfig(host, port)
		println("Setting new network config: $newConfig")
		appState.networkConfig.value = newConfig
	}
}
