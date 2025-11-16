package com.vincent.transfercloud

import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vincent.transfercloud.core.module.appModule
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.LabelLineSmall
import org.koin.compose.KoinApplication
import org.koin.dsl.module

@OptIn(ExperimentalMaterial3Api::class)
fun main() = application {

	val state = rememberWindowState(
		width = 1200.dp,
		height = 800.dp,
	)
	val scaffoldState = rememberBottomSheetScaffoldState(
		bottomSheetState = rememberStandardBottomSheetState(
			initialValue = SheetValue.Hidden,
			skipHiddenState = false
		),
		snackbarHostState = remember { SnackbarHostState() }
	)

	KoinApplication(application = { modules(appModule.plus(module { single { state } })) }
	) {
		Window(
			onCloseRequest = ::exitApplication,
			title = "Mail Client",
			state = state,
		) {
			CompositionLocalProvider(
				LocalBottomSheetScaffoldState provides scaffoldState,
				LocalTextStyle provides LabelLineSmall
			) {
				App()
			}
		}
	}
}