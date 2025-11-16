package com.vincent.transfercloud

import com.vincent.transfercloud.core.plugins.configDatabase
import com.vincent.transfercloud.core.plugins.configureRouting
import com.vincent.transfercloud.core.plugins.configureSerialization
import com.vincent.transfercloud.core.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
	EngineMain.main(args)
}

fun Application.module() {
	configureSerialization()
//	configureStatusPages()
	configDatabase()
	configureRouting()
}
