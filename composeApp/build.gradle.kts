import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.composeHotReload)
	alias(libs.plugins.serialization)
}

kotlin {
	jvm()

	sourceSets {
		commonMain.dependencies {
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			implementation(compose.ui)
			implementation(compose.materialIconsExtended)
			implementation(compose.components.resources)
			implementation(compose.components.uiToolingPreview)
			implementation(libs.kotlinx.serialization.json)
			implementation(libs.androidx.lifecycle.viewmodelCompose)
			implementation(libs.androidx.lifecycle.runtimeCompose)
			implementation(libs.material.expressive)

			implementation(libs.koin.compose)
			implementation(libs.koin.compose.viewmodel)
			implementation(libs.compottie)
			implementation(libs.voyager)
			implementation(libs.voyager.transitions)
			implementation(libs.coil.compose)
			implementation(libs.coil.network)
			implementation(libs.ktor.network)
			implementation(libs.faker)

			implementation(libs.ktor.client.core)
			implementation(libs.ktor.client.cio)
			implementation(libs.ktor.client.contentNegotiation)
			implementation(libs.ktor.client.logging)
			implementation(libs.ktor.serialization.kotlinx.json)

			implementation(libs.filekit.core)
			implementation(libs.filekit.dialogs)
			implementation(libs.filekit.dialogs.compose)

			implementation("net.jthink:jaudiotagger:3.0.1")
			implementation("ws.schild:jave-core:3.5.0")
			implementation("ws.schild:jave-native-win64:2.4.6")
			implementation(projects.shared)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
		jvmMain.dependencies {
			implementation(compose.desktop.currentOs)
			implementation(libs.kotlinx.coroutinesSwing)
			implementation(libs.splitpane.desktop)
		}
	}
}

repositories {
	google()
	mavenCentral()
}

compose.desktop {
	application {
		mainClass = "com.vincent.transfercloud.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "com.vincent.transfercloud"
			packageVersion = "1.0.0"

			buildTypes.release.proguard {
				configurationFiles.from(project.file("compose-desktop.pro"))
			}
		}
	}
}
