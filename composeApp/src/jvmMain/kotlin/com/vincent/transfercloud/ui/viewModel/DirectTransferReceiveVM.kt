package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.config.DirectTransferConfig
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.core.server.DirectTransferDto
import com.vincent.transfercloud.core.server.DirectTransferMeta
import com.vincent.transfercloud.core.server.DirectTransferSend
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.utils.getNetworkInterfaces
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.io.IOException
import java.io.File
import java.net.BindException

class DirectTransferReceiveVM(
	private val appState: AppState
) : ViewModel() {
	private val root = "C:\\TransferCloud\\DirectTransfer\\Received"
	private val selectorManager: SelectorManager = ActorSelectorManager(Dispatchers.IO)
	private var broadcastSocket: BoundDatagramSocket? = null
	private var transferSocket: ServerSocket? = null
	private var broadcastJob: Job? = null
	private val _receivedData = MutableStateFlow<Map<String, DirectTransferSend>>(emptyMap())
	val receivedData = _receivedData.asStateFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

	init {
		println("DirectTransferViewModel initialized")
		viewModelScope.launch(Dispatchers.IO) {
			try {
				transferSocket = aSocket(selectorManager).tcp().bind(null)
				println("Transfer Server is listening on port: ${transferSocket?.localAddress}")

				while (isActive) {
					val clientSocket = transferSocket!!.accept()
					println("Incoming direct transfer connection from: ${clientSocket.remoteAddress}")

					handleReceivingData(clientSocket)
				}
			} catch (e: BindException) {
				println("Port is already in use: ${e.message}")
			} catch (e: IOException) {
				println("Transfer Socket IOException: ${e.message}")
			} catch (e: Exception) {
				println("Transfer Socket Exception: ${e.javaClass.name} ${e.cause}")
			}
		}

		viewModelScope.launch(Dispatchers.IO) {
			try {
				broadcastSocket = aSocket(selectorManager).udp().bind(null) { broadcast = true }
				println("Receiver is running in: ${broadcastSocket?.localAddress}")
				broadcastJob = launch(Dispatchers.IO) {
					val payload = appState.currentUser.value?.let {
						DirectTransferDto(
							fromName = it.fullName,
							fromAvatar = it.avatarUrl!!,
							fromId = it.id,
							fromDeviceName = System.getProperty("os.name"),
							tcpHosts = getNetworkInterfaces().map { host -> host.toString().removePrefix("/") },
							tcpPort = transferSocket!!.localAddress.port(),
						)
					}
					val data = json.encodeToString(payload).encodeToByteArray()
					while (isActive && transferSocket != null) {
						val packet = Datagram(
							ByteReadPacket(data),
							InetSocketAddress(DirectTransferConfig.SUBNET_ADDRESS, DirectTransferConfig.DISCOVERY_PORT)
						)
						broadcastSocket?.send(packet)
						delay(2500)
					}
				}
			} catch (e: Exception) {
				println("Failed to bind UDP socket: ${e.javaClass.name} - ${e.message}")
			}
		}
	}

	private fun handleReceivingData(socket: Socket) = viewModelScope.launch(Dispatchers.IO) {
		try {
			val receiveChannel = socket.openReadChannel()
			val dtoString = receiveChannel.readUTF8Line()
			val fileDirs = mutableListOf<String>()
			if (dtoString != null) {
				val receivedDto = json.decodeFromString<DirectTransferSend>(dtoString)
				val cacheDir = "$root\\${receivedDto.id}"
				val file = File(cacheDir).apply { if (!exists()) mkdirs() }
				repeat(receivedDto.filesCount) {
					val meta = receiveChannel.readUTF8Line()
					if (meta == null) return@repeat
					val metaDto = json.decodeFromString<DirectTransferMeta>(meta)
					val outputFile = File(file, metaDto.fileName)
					val writeChanel = outputFile.writeChannel()
					receiveChannel.copyTo(writeChanel, limit = metaDto.fileSize)
					writeChanel.flushAndClose()
					fileDirs.add(outputFile.absolutePath)
				}
				_receivedData.update { currentMap ->
					receivedDto.files = fileDirs
					currentMap + (receivedDto.id to receivedDto)
				}
			}

			receiveChannel.cancel(IOException())
			socket.close()
		} catch (e: Exception) {
			println("Error handling received data: ${e.javaClass.name} - ${e.message}")
		}
	}

	fun deleteReceiveData(id: String) = viewModelScope.launch {
		_receivedData.update { currentMap ->
			currentMap - id
		}
		val dir = File("$root\\$id")
		if (dir.exists()) dir.deleteRecursively()
	}

	override fun onCleared() {
		super.onCleared()
		broadcastJob?.cancel()
		broadcastSocket?.close()
		transferSocket?.close()
		selectorManager.close()
		println("DirectTransferViewModel cleared and resources released")
		_receivedData.value.keys.forEach { id ->
			val dir = File("$root\\$id")
			if (dir.exists()) dir.deleteRecursively()
		}
	}
}