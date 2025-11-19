package com.vincent.transfercloud

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vincent.transfercloud.ui.screens.auth.AppGate
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.AppTheme
import com.vincent.transfercloud.ui.theme.HeadLineLarge
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
@Composable
fun App(
	appState: AppState = koinInject<AppState>(),
	viewModel: AppViewModel = koinInject<AppViewModel>()
) {
	val scaffoldState = LocalBottomSheetScaffoldState.current
	val theme by appState.darkTheme.collectAsState()
	AppTheme(darkTheme = theme) {
		BottomSheetScaffold(
			scaffoldState = scaffoldState,
			sheetPeekHeight = 0.dp,
			containerColor = MaterialTheme.colorScheme.surfaceContainer,
			sheetContent = @Composable {
				Column(
					Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text("Sheet Content", style = HeadLineLarge)
				}
			},
			snackbarHost = { SnackbarHost(hostState = scaffoldState.snackbarHostState) }
		) { innerPadding ->
			AppGate()
		}
	}
}