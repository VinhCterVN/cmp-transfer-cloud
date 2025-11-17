package com.vincent.transfercloud.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.theme.HeadLineMedium
import com.vincent.transfercloud.ui.viewModel.HomeViewModel
import org.koin.compose.koinInject

@Composable
fun HomeUI(
	appState: AppState = koinInject<AppState>(),
	viewModel: HomeViewModel = HomeViewModel()
) {
	val navigator = LocalNavigator.currentOrThrow
	val currentUser by appState.currentUser.collectAsState()

	Column(
		Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text("Welcome to TransferCloud!", style = HeadLineMedium)
		Spacer(Modifier.height(10.dp))

		ElevatedButton({
			navigator.push(FolderDetailView(currentUser?.rootFolderId!!))
		}) {
			Text("Go to your Drive")
		}
	}
}