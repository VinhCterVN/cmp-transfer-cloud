package com.vincent.transfercloud.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserInputDto(
	val id: String? = null,
	val fullName: String,
	val email: String,
	val password: String,
	val avatarUrl: String = "https://i.pravatar.cc/300",
)

@Serializable
data class UserOutputDto(
	val id: String,
	val fullName: String,
	val email: String,
	val avatarUrl: String? = null,
)
