package com.vincent.transfercloud.ui.state

import com.vincent.transfercloud.core.model.NetworkConfig
import com.vincent.transfercloud.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow

class AppState {
	val currentUser = MutableStateFlow<User?>(null)
	val networkConfig = MutableStateFlow(NetworkConfig())
	val isComposing = MutableStateFlow(false)
	val currentIndex = MutableStateFlow(EmailIndex.INBOX)
}

