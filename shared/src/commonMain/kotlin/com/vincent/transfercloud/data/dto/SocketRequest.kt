package com.vincent.transfercloud.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SocketRequest(
	val type: SocketRequestType,
	val payload: String,
)

@Serializable
data class SocketResponse(
	val status: ResponseStatus,
	val message: String,
	val data: String? = null,
)

@Serializable
data class GetRequest(
	val resource: String,
	val id: String? = null,
	val ownerId: String? = null
)

@Serializable
data class CreateRequest(
	val resource: String,
	val data: String
)

// DELETE HANDLERS
@Serializable
data class DeleteRequest(
	val resource: String,
	val id: String,
	val ownerId: String? = null
)

@Serializable
data class SearchRequest(
	val resource: String,
	val queryBy: String,
	val query: String
)

@Serializable
enum class SocketRequestType {
	LOGIN, REGISTER, LOGOUT, GET, CREATE, DELETE, UPDATE, SEARCH
}

