plugins {
	alias(libs.plugins.kotlinJvm)
	alias(libs.plugins.ktor)
	alias(libs.plugins.serialization)
	application
}

group = "com.vincent.transfercloud"
version = "1.0.0"
application {
	mainClass.set("com.vincent.transfercloud.ApplicationKt")
//	val isDevelopment: Boolean = project.ext.has("development")
	applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

dependencies {
	implementation(projects.shared)
	implementation(libs.ktor.serverCore)
	implementation(libs.ktor.serverNetty)
	implementation(libs.ktor.serialization.kotlinx.json)
	implementation(libs.ktor.serverContentNegotiation)
	implementation(libs.ktor.serverStatusPages)

	implementation(libs.ktor.exposed.core)
	implementation(libs.ktor.exposed.dao)
	implementation(libs.ktor.exposed.jdbc)
//	implementation(libs.ktor.exposed.datetime)
	implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")
	implementation(libs.ktor.h2Database)
	implementation(libs.postgresql)
	implementation(libs.mindrot.jbcrypt)

	implementation(libs.ktor.client.core)
	implementation(libs.ktor.client.cio)
	implementation(libs.ktor.client.contentNegotiation)

	implementation("io.github.serpro69:kotlin-faker:2.0.0-rc.11")
	testImplementation(libs.ktor.serverTestHost)
	testImplementation(libs.kotlin.testJunit)

	implementation("net.jthink:jaudiotagger:3.0.1")
	implementation("ws.schild:jave-core:3.5.0")
	implementation("ws.schild:jave-native-win64:2.4.6")

	implementation(libs.slf4j.simple)
}