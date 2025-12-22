package com.vincent.transfercloud.core.server

import java.security.MessageDigest
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object KeyManager {
	fun getFixedSecretKeyFromEnv(): SecretKey {
		val password = System.getenv("APP_ENCRYPTION_KEY")
			?: throw IllegalStateException("Environment variable APP_ENCRYPTION_KEY is not set")
		val sha = MessageDigest.getInstance("SHA-256")
		val keyBytes = sha.digest(password.toByteArray(Charsets.UTF_8))
		return SecretKeySpec(keyBytes, "AES")
	}
}