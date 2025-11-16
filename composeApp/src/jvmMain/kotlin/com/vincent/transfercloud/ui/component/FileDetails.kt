package com.vincent.transfercloud.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vincent.transfercloud.core.model.Email
import com.vincent.transfercloud.ui.theme.HeadLineMedium
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailsView(
	emailId: String,
	appState: AppState = koinInject<AppState>(),
	viewModel: AppViewModel = koinInject<AppViewModel>()
) {

	val navigator = LocalNavigator.currentOrThrow
	var emailDetail by remember { mutableStateOf<Email?>(null) }

	LaunchedEffect(Unit) {

	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(emailDetail?.title ?: "Email Detail", style = HeadLineMedium) },
				navigationIcon = {
					IconButton(onClick = { navigator.pop() }) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				},
				actions = {
					IconButton({}) {Icon(Icons.Default.Delete, contentDescription = "Delete Email")}
					IconButton({}) { Icon(Icons.Default.MoreVert, contentDescription = "More Options") }
				}
			)
		}
	) { paddingValues ->
		emailDetail?.let { emailData ->
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues)
					.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(text = "From: ${emailData.from}", style = MaterialTheme.typography.titleLarge)
				Text(text = "Title: ${emailData.title}", style = MaterialTheme.typography.titleMedium)
				Text(text = "Date: ${emailData.createdAt.toDateString()}", style = MaterialTheme.typography.bodyMedium)
				HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
				Text(text = "Content:", style = MaterialTheme.typography.titleMedium)
				Text(text = emailData.content, style = MaterialTheme.typography.bodyLarge)
			}
		} ?: run {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator() // Show loading while fetching email
			}
		}
	}
}
