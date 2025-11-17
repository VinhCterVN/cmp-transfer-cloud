package com.vincent.transfercloud.data.enum

import kotlinx.serialization.Serializable

@Serializable
enum class SharePermission {
	VIEW,
	EDIT,
	OWNER
}

@Serializable
enum class FileLocation {
	LOCAL,
	CLOUD
}
