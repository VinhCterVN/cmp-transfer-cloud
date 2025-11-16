package com.vincent.transfercloud.ui.viewModel

import androidx.lifecycle.ViewModel
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
	suspend fun login(email: String, password: String) {
		if (email.trim().isEmpty() || password.isEmpty()) return
		val res = client.post("http://localhost:8080/api/auth/login") {
			contentType(ContentType.Application.Json)
			setBody(LoginRequestDto(email, password))
		}.bodyAsText()
		if (res.isEmpty()) return
		val user = json.decodeFromString<UserOutputDto>(res)
		println("Logging in with ${user.fullName}")
		appState.currentUser.emit(user)
	}

	suspend fun register(fullName: String, email: String, password: String) {
		if (fullName.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) return
		val res = client.post("http://localhost:8080/api/auth/register") {
			contentType(ContentType.Application.Json)
			setBody(UserInputDto(fullName, email, password))
		}.bodyAsText()
		if (res.isEmpty()) return
		val user = json.decodeFromString<UserOutputDto>(res)
		println("Register: ${user.fullName}")
		appState.currentUser.emit(user)
	}
}