package com.vincent.transfercloud.core.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import java.net.BindException
import java.util.concurrent.ConcurrentHashMap

object Server {
	private const val PORT = 9090
	val selectorManager = ActorSelectorManager(Dispatchers.IO)
	val clients = ConcurrentHashMap<String, ClientHandler>()
	val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	var server: BoundDatagramSocket? = null

	fun run() = runBlocking {
		try {
			aSocket(selectorManager).tcp().bind("0.0.0.0", PORT).use { ss ->
				println("Server listening on $PORT")
				while (true) {
					val clientSocket = ss.accept()
					println("Client connected: ${clientSocket.remoteAddress}")
					ClientHandler(clientSocket, this@Server, scope).start()
					delay(1000)
				}
			}
		} catch (e: BindException) {
			println("Port $PORT is already in use. Please choose another port - ${e.message}\n")
			return@runBlocking
		} catch (e: Exception) {
			println("Server Exception: ${e.message}\n")
		}
	}

	fun register(userId: String, handler: ClientHandler): Boolean =
		clients.put(userId, handler) == null

	fun logout(userId: String): Boolean =
		clients.remove(userId) != null

	fun stop() {
		scope.cancel()
		selectorManager.close()
		server?.close()
		println("Server stopped.")
	}
}