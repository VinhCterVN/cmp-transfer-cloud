package com.vincent.transfercloud.data.dto

import com.vincent.transfercloud.data.enum.SharePermission
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SocketRequest(
	val id: String = UUID.randomUUID().toString(),
	val type: SocketRequestType,
	val payload: String,
)

@Serializable
data class SocketResponse(
	val id: String = UUID.randomUUID().toString(),
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

@Serializable
data class UpdateRequest(
	val id: String,
	val resource: String,
	val ownerId: String,
	val data: String
)

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
data class DownloadRequest(
	val id: String,
	val ownerId: String,
	val resource: String,
)

@Serializable
data class MoveRequest(
    val resource: String, 
    val id: String,       
    val targetParentId: String, 
    val ownerId: String   
)

@Serializable
data class ShareRequest(
	val resourceId: String,
	val ownerId: String,
	val shareToEmail: String,
	val permission: SharePermission,
	val resource: String
)

@Serializable
enum class SocketRequestType {
	LOGIN, REGISTER, LOGOUT, GET, CREATE, DELETE, UPDATE, SEARCH, DOWNLOAD, MOVE, COPY, SHARE
}

