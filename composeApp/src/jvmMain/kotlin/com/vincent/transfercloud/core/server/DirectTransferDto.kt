package com.vincent.transfercloud.core.server

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID
import kotlin.time.ExperimentalTime

@Serializable
data class DirectTransferDto(
	val fromName: String,
	val fromAvatar: String,
	val fromId: String,
	val fromDeviceName: String,
	val tcpHosts: List<String>,
	val tcpPort: Int,
)

@Serializable
data class DiscoveredDevice(
	val info: DirectTransferDto,
	val lastSeen: Long = System.currentTimeMillis() // Thời điểm nhận gói tin cuối cùng
)

@Serializable
data class DirectTransferSend @OptIn(ExperimentalTime::class) constructor(
	val id: String = UUID.randomUUID().toString(),
	val fromName: String,
	val fromAvatar: String,
	val fromId: String,
	val toId: String,
	val filesCount: Int,
	var files: List<String>,
	val totalSize: Long,
	val transferTime: Long = Instant.now().epochSecond
)

@Serializable
data class DirectTransferMeta(
	val fromId: String,
	val fileName: String,
	val fileSize: Long,
	val mimeType: String
)

@Serializable
data class DirectTransferFile(
	val fileNameWithExtension: String,
	val fileSize: Long,
	val mimeType: String,
	val data: ByteArray,
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as DirectTransferFile

		if (fileSize != other.fileSize) return false
		if (fileNameWithExtension != other.fileNameWithExtension) return false
		if (mimeType != other.mimeType) return false
		if (!data.contentEquals(other.data)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = fileSize.hashCode()
		result = 31 * result + fileNameWithExtension.hashCode()
		result = 31 * result + mimeType.hashCode()
		result = 31 * result + data.contentHashCode()
		return result
	}
}

