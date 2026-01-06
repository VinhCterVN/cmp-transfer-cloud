package com.vincent.transfercloud.ui.state

import com.vincent.transfercloud.core.model.NetworkConfig
import com.vincent.transfercloud.core.model.TransferConfig
import com.vincent.transfercloud.data.dto.BreadcrumbItem
import com.vincent.transfercloud.data.dto.UserOutputDto
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.MutableStateFlow

class AppState {
	val darkTheme = MutableStateFlow(false)
	val isConnected = MutableStateFlow(false)
	val currentTab = MutableStateFlow<AppTab>(AppTab.HOME)
	val currentUser = MutableStateFlow<UserOutputDto?>(null)
	val networkConfig = MutableStateFlow(NetworkConfig())
	val transferConfig = MutableStateFlow(TransferConfig())
	val isCreatingFolder = MutableStateFlow(false)

	val renamingFolder = MutableStateFlow("" to "")
	val isRenamingFolder = MutableStateFlow(true)

	val sharingFolder = MutableStateFlow("" to false)
	val breadcrumb = MutableStateFlow<List<BreadcrumbItem>>(emptyList())
	val currentFolder = MutableStateFlow("")
	val clientSocket = MutableStateFlow<Socket?>(null)
	val clientSocketWriteChannel = MutableStateFlow<ByteWriteChannel?>(null)
	val clientSocketReadChannel = MutableStateFlow<ByteReadChannel?>(null)
	val fileDetailShow = MutableStateFlow(false)
	enum class AppTab {
		HOME, MY_DRIVE, SHARED, TRANSFER, TRASH
	}
}

