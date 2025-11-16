package com.vincent.transfercloud.ui.state

import com.vincent.transfercloud.core.model.NetworkConfig
import com.vincent.transfercloud.data.dto.UserOutputDto
import kotlinx.coroutines.flow.MutableStateFlow

class AppState {
	val darkTheme = MutableStateFlow(false)
	val currentUser = MutableStateFlow<UserOutputDto?>(null)
	val networkConfig = MutableStateFlow(NetworkConfig())
	val isComposing = MutableStateFlow(false)
	val currentIndex = MutableStateFlow(EmailIndex.INBOX)
}

