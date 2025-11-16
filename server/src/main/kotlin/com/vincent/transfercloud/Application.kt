package com.vincent.transfercloud

import com.vincent.transfercloud.core.plugins.configDatabase
import com.vincent.transfercloud.core.plugins.configureRouting
import com.vincent.transfercloud.core.plugins.configureSerialization
import com.vincent.transfercloud.core.plugins.configureStatusPages
import com.vincent.transfercloud.core.server.Server
import io.ktor.server.application.*
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>): Unit = runBlocking {
	launch(Dispatchers.IO) { Server.run() }
	EngineMain.main(args)
}

fun Application.module() {
	configureSerialization()
	configureStatusPages()
	configDatabase()
	configureRouting()

	environment.monitor.subscribe(ApplicationStopped) {
		println("Application stopping...")
		Server.stop()
	}
}
