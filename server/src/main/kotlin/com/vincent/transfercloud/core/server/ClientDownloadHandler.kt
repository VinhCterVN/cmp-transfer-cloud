package com.vincent.transfercloud.core.server

import com.vincent.transfercloud.data.repository.FolderRepository
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.io.discardingSink
import java.io.File
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ClientDownloadHandler {
	private val selectorManager = ActorSelectorManager(Dispatchers.IO)
	private var socket: ServerSocket? = null
	private const val PORT = 26789
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	init {
		initSocket()
	}

	fun init() {
		println("ClientDownloadHandler initialized.")
	}

	fun initSocket() = scope.launch {
		if (socket != null) {
			socket?.close()
		}
		socket = aSocket(selectorManager).tcp().bind(hostname = "0.0.0.0", port = PORT)
		println("Client Download Server started on port $PORT")
	}

	fun getAddress()  = socket?.localAddress

	fun handleClientDownload(folderId: String) = scope.launch {
		val uuid = UUID.fromString(folderId)
		val entries = FolderRepository.getAllFilesWithCTE(uuid)
		println("Loaded entries: ${entries.size} files to download.")
		try {
			while (isActive) {
				if (socket == null) {
					println("Socket is not initialized.")
					return@launch
				}
				val client = socket!!.accept()
				println("Client connected: ${client.remoteAddress}")
				val writeChannel = client.openWriteChannel(autoFlush = true)
				val outputStream = writeChannel.toOutputStream()
				val zipOut = ZipOutputStream(outputStream)

				for (entry in entries) {
					val file = File(storageDir, entry.storagePath)
					if (!file.exists()) continue

					val decriptedFile = FileDecryptor.loadAndDecryptFile(entry.storagePath, KeyManager.getFixedSecretKeyFromEnv())

					val tempFile = File.createTempFile("decrypted_", null)
					tempFile.outputStream().use { it.write(decriptedFile) }

					val zipEntry = ZipEntry(entry.entryPath)
					zipOut.putNextEntry(zipEntry)

//					file.inputStream().use { it.copyTo(zipOut) }
					tempFile.inputStream().use { it.copyTo(zipOut) }

					zipOut.closeEntry()
					tempFile.delete()
				}
				zipOut.finish()
				zipOut.close()
				outputStream.flush()
				break
			}
		} catch (e: Exception) {
			println("Error in client download handler: ${e.message}")
			stop()
		}
	}

	fun stop() = scope.launch {
		socket?.close()
		selectorManager.close()
		scope.cancel()
		println("Client Download Server stopped.")
	}
}