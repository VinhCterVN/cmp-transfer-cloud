package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.ui.state.AppState
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.io.IOException
import java.io.File
import java.net.ConnectException

class AppViewModel(
	appState: AppState
) : BaseSocketViewModel(appState) {
	private val root = "C:${File.separator}TransferCloud${File.separator}"
	private val selectorManager: SelectorManager = ActorSelectorManager(Dispatchers.IO)
	private val socket = appState.clientSocket
	private var reconnectJob: Job? = null
	private var reconnectAttempts = 0
	private val maxReconnectAttempts = 10
	private val baseReconnectDelayMs = 2000L

	init {
		connect()
		viewModelScope.launch {
			appState.currentUser.collect {
				it?.let {
					val file = File("$root${it.fullName}").apply { if (!exists()) mkdirs() }
					println("User directory ensured at: ${file.absolutePath}")
				}
			}
		}
	}

	private fun connect() = viewModelScope.launch {
		try {
			reconnectJob?.cancel()
			disconnect(false)
			socket.value = aSocket(selectorManager).tcp().connect("localhost", 9090)
			sendChannel.value = socket.value!!.openWriteChannel(autoFlush = true)
			receiveChannel.value = socket.value!!.openReadChannel()

			if (receiveChannel.value != null) {
				println("Connected successfully}")
			}

			reconnectAttempts = 0

		} catch (e: ConnectException) {
			println("Cannot connect to server: ${e.message}")
			scheduleReconnect()
		} catch (e: Exception) {
			println("Connection error: ${e.message}")
			scheduleReconnect()
		}
	}

	private suspend fun reconnectWithNewConfig() {
		val currentUser = appState.currentUser.value

		disconnect(closeSelector = false)
		connect()

		currentUser?.let { user ->
			delay(500)
			println("Auto-login after config change")
		}
	}

	private fun scheduleReconnect() {
		reconnectJob?.cancel()
		if (reconnectAttempts < maxReconnectAttempts) {
			val delayMs = baseReconnectDelayMs * (1 shl reconnectAttempts)
			reconnectAttempts++

			println("Scheduling reconnect attempt $reconnectAttempts in ${delayMs}ms")
			reconnectJob = viewModelScope.launch {
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

	fun logout() {
		appState.currentUser.value = null
	}

	private suspend fun disconnect(closeSelector: Boolean = true) = viewModelScope.async {
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
	}.await()
}
