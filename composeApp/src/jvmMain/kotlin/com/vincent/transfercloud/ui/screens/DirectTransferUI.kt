package com.vincent.transfercloud.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vincent.transfercloud.core.server.DirectTransferSend
import com.vincent.transfercloud.ui.navigation.DirectTransferSendScreen
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.theme.HeadLineLarge
import com.vincent.transfercloud.ui.viewModel.DirectTransferReceiveVM
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import transfercloud.composeapp.generated.resources.Res

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DirectTransferUI(
	appState: AppState = koinInject<AppState>(),
	viewModel: DirectTransferReceiveVM = koinViewModel()
) {
	val navigator = LocalNavigator.currentOrThrow
	val expanded by rememberSaveable { mutableStateOf(true) }
	val receivedData by viewModel.receivedData.collectAsState()
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes("files/transfer.json").decodeToString()
		)
	}

	Box(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp).background(MaterialTheme.colorScheme.surface)) {
		LazyColumn(
			Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			item {
				Text(
					"Direct Receiving Data... ${receivedData.size}",
					style = HeadLineLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			item {
				Image(
					painter = rememberLottiePainter(
						composition = composition,
						iterations = Compottie.IterateForever
					),
					contentDescription = null,
					modifier = Modifier.fillMaxWidth(0.66f).aspectRatio(1f)
				)
			}
		}
		HorizontalFloatingToolbar(
			modifier = Modifier.align(Alignment.BottomCenter),
			expanded = expanded,
			leadingContent = { LeadingContent() },
			trailingContent = { TrailingContent(receivedData) },
			content = {
				TooltipBox(
					state = rememberTooltipState(),
					positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
					tooltip = { PlainTooltip { Text("Clear selected") } },
				) {
					FilledIconButton(
						modifier = Modifier.width(64.dp),
						onClick = { navigator.push(DirectTransferSendScreen) }
					) {
						Icon(Icons.Filled.Add, null)
					}
				}
			},
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeadingContent() {
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description") } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { /* doSomething() */ }) {
			Icon(Icons.Filled.Check, contentDescription = "Localized description")
		}
	}
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description") } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { /* doSomething() */ }) {
			Icon(Icons.Filled.Edit, contentDescription = "Localized description")
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrailingContent(
	data: Map<String, DirectTransferSend>
) {
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description") } },
		state = rememberTooltipState(),
	) {
		Box(contentAlignment = Alignment.Center) {
			IconButton(onClick = { /* doSomething() */ }) {
				Icon(Icons.Filled.History, contentDescription = "History")
			}
			if (data.isNotEmpty()) {
				Badge(
					containerColor = Color.Red,
					contentColor = Color.White,
					modifier = Modifier
						.align(Alignment.TopEnd)
						.offset(x = (-4).dp, y = 4.dp)
				) {
					Text(data.size.toString())
				}
			}
		}
	}
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description") } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { /* doSomething() */ }) {
			Icon(Icons.Filled.Favorite, contentDescription = "Localized description")
		}
	}
}