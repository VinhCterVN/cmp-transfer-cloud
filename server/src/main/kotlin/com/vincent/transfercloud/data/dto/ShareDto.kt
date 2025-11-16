package com.vincent.transfercloud.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShareInputDto(
    val fileId: String,
    val ownerId: String,
    val sharedWithUserId: String,
)

@Serializable
data class ShareOutputDto(
    val id: String,
    val fileId: String,
    val ownerId: String,
    val sharedWithUserId: String,
)
