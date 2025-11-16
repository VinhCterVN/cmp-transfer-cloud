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
	implementation(libs.logback)
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

	testImplementation(libs.ktor.serverTestHost)
	testImplementation(libs.kotlin.testJunit)
}