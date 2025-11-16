plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.serialization)
}

kotlin {
	jvm()

	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlinx.serialization.json)
		}
		jvmMain.dependencies {
			implementation(libs.kotlinx.datetime)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
}

