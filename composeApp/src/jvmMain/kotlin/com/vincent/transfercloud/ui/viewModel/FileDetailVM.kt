package com.vincent.transfercloud.ui.viewModel

import com.vincent.transfercloud.ui.state.AppState

class FileDetailVM(
	appState: AppState
) : BaseSocketViewModel(appState) {
	init {
		println("FileDetailViewModel created")
	}

	override fun onCleared() {
		super.onCleared()
		println("FileDetailViewModel cleared")
	}
}