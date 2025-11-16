package com.vincent.transfercloud.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
	val email: String,
	val password: String
)

@Serializable
data class RegisterRequestDto(
	val fullName: String,
	val email: String,
	val password: String,
	val avatarUrl: String = "https://i.pravatar.cc/300"
)