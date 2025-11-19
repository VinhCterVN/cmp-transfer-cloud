package com.vincent.transfercloud.ui.viewModel

import com.vincent.transfercloud.core.constant.json
import com.vincent.transfercloud.data.dto.LoginRequest
import com.vincent.transfercloud.data.dto.SocketRequestType
import com.vincent.transfercloud.data.dto.UserInputDto
import com.vincent.transfercloud.data.dto.UserOutputDto
import com.vincent.transfercloud.ui.state.AppState

class AuthViewModel(
	appState: AppState
) : BaseSocketViewModel(appState = appState) {
	fun login(email: String, password: String): String? {
		if (email.isBlank() || password.isBlank()) return "Email and password cannot be empty"
		var res: String? = ""
		sendRequest(
			type = SocketRequestType.LOGIN,
			payload = LoginRequest(email, password),
			onSuccess = { res ->
				val user = json.decodeFromString<UserOutputDto>(res.data!!)
				appState.currentUser.value = user
			},
			onError = { msg ->
				println("Login failed: $msg")
				res = "Login failed: $msg"
			}
		)
		return res
	}

	fun register(fullName: String, email: String, password: String): String? {
		if (email.isBlank() || password.isBlank()) return "Email and password cannot be empty"
		var res: String? = ""
		sendRequest(
			type = SocketRequestType.REGISTER,
			payload = UserInputDto(fullName, email, password),
			onSuccess = { res ->
				val user = json.decodeFromString<UserOutputDto>(res.data!!)
				appState.currentUser.value = user
			},
			onError = { msg ->
				println("Register failed: $msg")
				res = "Register failed: $msg"
			}
		)
		return res
	}
}