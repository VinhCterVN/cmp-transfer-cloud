package com.vincent.transfercloud.data.helper

import org.mindrot.jbcrypt.BCrypt

fun hashPassword(password: String): String {
	val salt = BCrypt.gensalt(12)
	return BCrypt.hashpw(password, salt)
}

fun verifyPassword(password: String, hashedPassword: String): Boolean {
	return BCrypt.checkpw(password, hashedPassword)
}