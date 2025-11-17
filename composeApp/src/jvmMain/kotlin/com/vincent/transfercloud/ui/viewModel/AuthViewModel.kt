package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.ViewModel
import com.vincent.transfercloud.SERVER_URL
import com.vincent.transfercloud.core.constant.client
import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.data.dto.LoginRequestDto
import com.vincent.transfercloud.data.dto.UserInputDto
import com.vincent.transfercloud.data.dto.UserOutputDto
import com.vincent.transfercloud.ui.state.AppState
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class AuthViewModel(
	private val appState: AppState
) : ViewModel() {
	suspend fun login(email: String, password: String): String? {
		try {
			if (email.trim().isEmpty() || password.isEmpty()) return "Email and password cannot be empty"
			val res = client.post("$SERVER_URL/auth/login") {
				contentType(ContentType.Application.Json)
				setBody(LoginRequestDto(email, password))
			}.bodyAsText()
			if (res.isEmpty()) return "Invalid email or password"
			val user = json.decodeFromString<UserOutputDto>(res)
			println("Logging in with ${user.fullName}")
			appState.currentUser.emit(user)
		} catch (e: Exception) {
			println("Login failed: ${e.message}")
			return "Login failed: ${e.message}"
		}
		return null
	}

	suspend fun register(fullName: String, email: String, password: String): String? {
		try {
			if (fullName.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) return "All fields are required"
			val res = client.post("$SERVER_URL/auth/register") {
				contentType(ContentType.Application.Json)
				setBody(UserInputDto(fullName, email, password))
			}.bodyAsText()
			if (res.isEmpty()) return "Registration failed"
			val user = json.decodeFromString<UserOutputDto>(res)
			println("Register: ${user.fullName}")
			appState.currentUser.emit(user)
		} catch (e: Exception) {
			println("Registration failed: ${e.message}")
			return "Registration failed: ${e.message}"
		}
		return null
	}
}