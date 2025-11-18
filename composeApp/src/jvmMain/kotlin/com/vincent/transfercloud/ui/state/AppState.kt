package com.vincent.transfercloud.ui.state

import com.vincent.transfercloud.core.model.NetworkConfig
import com.vincent.transfercloud.data.dto.BreadcrumbItem
import com.vincent.transfercloud.data.dto.UserOutputDto
import kotlinx.coroutines.flow.MutableStateFlow

class AppState {
	val darkTheme = MutableStateFlow(false)
	val currentUser = MutableStateFlow<UserOutputDto?>(null)
	val networkConfig = MutableStateFlow(NetworkConfig())
	val isCreatingFolder = MutableStateFlow(false)
	val breadcrumb = MutableStateFlow<List<BreadcrumbItem>>(emptyList())
	val currentFolder = MutableStateFlow("")
	val currentViewIndex = MutableStateFlow(FileViewIndex.GRID)
}

