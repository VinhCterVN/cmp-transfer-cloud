package com.vincent.transfercloud.ui.state

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.compositionLocalOf

@ExperimentalMaterial3Api
val LocalBottomSheetScaffoldState = compositionLocalOf<BottomSheetScaffoldState> {
	error("No BottomSheet state provided")
}

enum class FileViewIndex {
	LIST, GRID
}

sealed class UIState {
	object Loading : UIState()
	object Ready : UIState()
	data class Error(val message: String) : UIState()
}