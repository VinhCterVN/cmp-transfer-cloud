package com.vincent.transfercloud.ui.state

sealed class UIState {
	object Loading : UIState()
	object Ready : UIState()
	data class Error(val message: String) : UIState()
}