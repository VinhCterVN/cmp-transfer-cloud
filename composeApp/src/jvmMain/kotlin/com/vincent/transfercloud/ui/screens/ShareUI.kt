package com.vincent.transfercloud.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.viewModel.ShareViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareScreen(
	appState: AppState = koinInject<AppState>(),
	viewModel: ShareViewModel = koinViewModel()
) {
	val uiState by viewModel.uiState.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.getSharedData()
	}

	Box(Modifier.fillMaxSize()) {
		when (uiState) {
			is UIState.Loading -> @Composable {
				Box(
					Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					ContainedLoadingIndicator(modifier = Modifier.size(150.dp))
				}
			}
			is UIState.Error -> @Composable {
				Text(text = (uiState as UIState.Error).message, modifier = Modifier.align(Alignment.Center))
			}
			else -> @Composable {
				LazyColumn(
					Modifier.fillMaxSize().padding(4.dp)
				) {
					stickyHeader {
						Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
							Text("Shared with you", style = TitleLineBig)
						}
					}
				}
			}
		}
	}
}