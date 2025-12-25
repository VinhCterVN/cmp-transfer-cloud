package com.vincent.transfercloud.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetFolderContentsRequestDto(
	val folderId: String,
	val status: ResponseStatus,
	val message: String,
	val data: FolderWithContentsDto?
)

@Serializable
data class GetSharedDataResponse(
	val status: ResponseStatus,
	val message: String,
	val folders: List<FolderOutputDto>,
	val files: List<FileOutputDto>
)

@Serializable
enum class ResponseStatus{
	SUCCESS,
	ERROR
}

@Serializable
data class CreateFolderResponseDto(
	val folder: FolderOutputDto?,
	val status: ResponseStatus,
	val message: String
)

@Serializable
data class CreateFileResponseDto(
	val file: FileOutputDto?,
	val status: ResponseStatus,
	val message: String
)

@Serializable
data class DownloadFileResource(
	val fileName: String,
	val ownerId: String,
	val mimeType: String,
	val data: ByteArray
)

@Serializable
data class GetThumbnailResponseDto(
	val fileId: String,
	val status: ResponseStatus,
	val message: String,
	val thumbnailBytes: ByteArray?
)