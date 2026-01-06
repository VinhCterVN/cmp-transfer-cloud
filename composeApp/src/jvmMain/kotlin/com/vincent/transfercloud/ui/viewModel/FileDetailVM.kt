package com.vincent.transfercloud.ui.viewModel

import com.vincent.transfercloud.core.server.SocketRepository
import com.vincent.transfercloud.ui.state.AppState

class FileDetailVM(
	appState: AppState,
	socketRepository: SocketRepository
) : BaseSocketViewModel(appState, socketRepository) {
	init {
		println("FileDetailViewModel created")
	}

	override fun onCleared() {
		super.onCleared()
		println("FileDetailViewModel cleared")
	}
}