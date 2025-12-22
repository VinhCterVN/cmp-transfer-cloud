package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vincent.transfercloud.core.config.DirectTransferConfig
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.core.server.DirectTransferDto
import com.vincent.transfercloud.core.server.DirectTransferMeta
import com.vincent.transfercloud.core.server.DirectTransferSend
import com.vincent.transfercloud.core.server.DiscoveredDevice
import com.vincent.transfercloud.ui.state.AppState
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okio.IOException
import java.io.File
import java.net.InetSocketAddress

class DirectTransferSendVM(
	private val appState: AppState
) : ViewModel() {
	private val selectorManager: SelectorManager = ActorSelectorManager(Dispatchers.IO)
	private var discoverySocket: BoundDatagramSocket? = null
	private val _uploadingFiles = MutableStateFlow<List<File>>(emptyList())
	val uploadingFiles = _uploadingFiles.asStateFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
	private val _receiversMap = MutableStateFlow<Map<String, DiscoveredDevice>>(emptyMap())
	val availableReceivers: StateFlow<List<DirectTransferDto>> = _receiversMap
		.map { it.values.map { device -> device.info }.sortedBy { e -> e.fromName } }
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
	private val _isUploading = MutableStateFlow(false)
	val isUploading = _isUploading.asStateFlow()
	private val _uploadingToId = MutableStateFlow("")
	val uploadingToId = _uploadingToId.asStateFlow()
	private val _sendingProgress = MutableStateFlow(0 to 0)
	val sendingProgress = _sendingProgress.asStateFlow()
	private val _bytesProgress = MutableStateFlow(0L to 0L)
	val bytesProgress = _bytesProgress.asStateFlow()

	init {
		startDiscovery()
		startCleanupJob()
	}

	private fun startDiscovery() {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				val socketAddress = InetSocketAddress(DirectTransferConfig.DISCOVERY_PORT)

				discoverySocket = aSocket(selectorManager).udp().bind(port = socketAddress.port) {
					reuseAddress = true
					broadcast = true
				}
				println("Sender is listening for broadcasts on port ${DirectTransferConfig.DISCOVERY_PORT}")

				while (isActive) {
					val packet = discoverySocket!!.receive()
					try {
						val text = packet.packet.readText()
						val dto = json.decodeFromString<DirectTransferDto>(text)
						if (dto.fromId == appState.currentUser.value?.id) continue
						_receiversMap.update { currentMap ->
							currentMap + (dto.fromId to DiscoveredDevice(dto))
						}
					} catch (e: Exception) {
						println("Error parsing packet: ${e.message}")
						continue
					}
				}
			} catch (e: Exception) {
				println("Discovery failed: ${e.message}")
			}
		}
	}

	private fun startCleanupJob() {
		viewModelScope.launch {
			while (isActive) {
				delay(5000)
				val now = System.currentTimeMillis()
				val timeoutThreshold = 5000L
				_receiversMap.update { currentMap ->
					currentMap.filterValues { device ->
						(now - device.lastSeen) < timeoutThreshold
					}
				}
			}
		}
	}

	fun transferTo(device: DirectTransferDto) = viewModelScope.launch {
		try {
			_isUploading.emit(true)
			_uploadingToId.emit(device.fromId)
			val socket = aSocket(selectorManager).tcp().connect(device.tcpHost, device.tcpPort)
			val writeChannel = socket.openWriteChannel(autoFlush = true)
			val dto = appState.currentUser.value?.let {
				DirectTransferSend(
					fromName = it.fullName,
					fromAvatar = it.avatarUrl!!,
					fromId = it.id,
					toId = device.fromId,
					filesCount = _uploadingFiles.value.size,
					files = _uploadingFiles.value.map { file -> file.name },
					totalSize = _uploadingFiles.value.sumOf { file -> file.length() }
				)
			}
			val payload = json.encodeToString(dto)
			writeChannel.writeStringUtf8("$payload\n")
			val totalFiles = _uploadingFiles.value.size
			val totalBytes = _uploadingFiles.value.sumOf { it.length() }
			var sentBytes = 0L

			_sendingProgress.emit(0 to totalFiles)
			_bytesProgress.emit(0L to totalBytes)

			repeat(totalFiles) { index ->
				val file = _uploadingFiles.value[index]
				val meta = DirectTransferMeta(
					fromId = dto!!.fromId,
					fileName = file.name,
					fileSize = file.length(),
					mimeType = file.getMimeType()
				)
				val jsonLine = json.encodeToString(meta)
				writeChannel.writeStringUtf8("$jsonLine\n")
				val fileInputChannel = file.readChannel()
				try {
					val bufferSize = 8192
					val buffer = ByteArray(bufferSize)
					var bytesRead: Int
					var fileSentBytes = 0L

					while (fileInputChannel.readAvailable(buffer, 0, bufferSize).also { bytesRead = it } > 0) {
						writeChannel.writeFully(buffer, 0, bytesRead)
						fileSentBytes += bytesRead
						sentBytes += bytesRead

						_bytesProgress.emit(sentBytes to totalBytes)
					}
					delay(300)
					_sendingProgress.emit((index + 1) to totalFiles)
					println("Sent file: ${file.name} (${file.length()} bytes)")
				} finally {
					fileInputChannel.cancel()
				}
			}

			println("Finished sending $totalFiles files ($totalBytes bytes) to ${device.tcpHost}:${device.tcpPort}")
			_sendingProgress.emit(0 to 0)
			_bytesProgress.emit(0L to 0L)
			_uploadingToId.emit("")
			_isUploading.emit(false)
			writeChannel.cancel(IOException())
			socket.close()
		} catch (e: Exception) {
			println("Error transferring files: ${e.javaClass.name} ${e.message}")
			_isUploading.emit(false)
		}
	}

	fun addTransferFile(file: File) = _uploadingFiles.update { currentList ->
		if (!currentList.contains(file)) {
			currentList + file
		} else {
			currentList
		}
	}

	fun addTransferFiles(files: List<File>) = _uploadingFiles.update { currentList ->
		files.forEach { println("Adding file for transfer: ${it.absolutePath}") }
		currentList + files.filter { !currentList.contains(it) }
	}

	fun removeTransferFile(file: File) = _uploadingFiles.update { currentList ->
		currentList.filter { it != file }
	}

	fun clearTransferFiles() {
		_uploadingFiles.value = emptyList()
	}

	override fun onCleared() {
		super.onCleared()
		discoverySocket?.close()
		selectorManager.close()
	}
}