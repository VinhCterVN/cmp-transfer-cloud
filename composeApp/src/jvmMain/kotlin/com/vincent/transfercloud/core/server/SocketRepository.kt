package com.vincent.transfercloud.core.server

import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.data.dto.SocketResponse
import com.vincent.transfercloud.ui.state.AppState
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SocketRepository(private val appState: AppState) {
    private val _socketResponseFlow = MutableSharedFlow<SocketResponse>()
    val socketResponseFlow: SharedFlow<SocketResponse> = _socketResponseFlow.asSharedFlow()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        startListening()
    }

    private fun startListening() {
        scope.launch {
            while (appState.clientSocketReadChannel.value == null) {
                delay(100)
            }

            val input = appState.clientSocketReadChannel.value!!
            println("Socket listening started")

            try {
                while (!input.isClosedForRead) {
                    val line = input.readUTF8Line()
                    if (line != null) {
                        val text = line.trimIndent()
                        if (text.isNotEmpty()) {
                            try {
                                val res = json.decodeFromString<SocketResponse>(text)
                                _socketResponseFlow.emit(res)
                            } catch (e: Exception) {
                                println("Parse error: ${e.message}")
                            }
                        }
                    } else {
                        break // Socket closed
                    }
                }
            } catch (e: Exception) {
                 println("Socket read error: ${e.message}")
            }
        }
    }

    suspend fun writeToSocket(text: String) = appState.clientSocketWriteChannel.value?.writeStringUtf8(text)
}