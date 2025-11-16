package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.model.User
import com.vincent.transfercloud.ui.state.AppState
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class AppViewModel(
	private val appState: AppState
) : ViewModel() {
	private val root = "C:${File.separator}MailServer${File.separator}"
	private val json: Json = Json {
		ignoreUnknownKeys = true
		encodeDefaults = true
	}
	private val selectorManager: SelectorManager = ActorSelectorManager(Dispatchers.IO)
	private var socket: BoundDatagramSocket? = null

	init {
		viewModelScope.launch {
			try {
				socket = aSocket(selectorManager).udp().bind(null)
			} catch (e: Exception) {
				println("ViewModel Error: ${e.message} - ${e.javaClass.name}")
			}
		}
		viewModelScope.launch {
			appState.currentUser.collect {
				it?.let {
					val file = File("$root${it.name}").apply { if (!exists()) mkdirs() }
					println("User directory ensured at: ${file.absolutePath}")
				}
			}
		}
	}

	override fun onCleared() {
		super.onCleared()
		socket?.close()
		selectorManager.close()
		println("AppViewModel cleared and resources released.")
	}

	fun login(username: String, password: String) = viewModelScope.launch {
		appState.currentUser.emit(User("Vincent"))
	}

	fun register(username: String, email: String, password: String) = viewModelScope.launch {}

	fun logout() {
		appState.currentUser.value = null
	}
}
