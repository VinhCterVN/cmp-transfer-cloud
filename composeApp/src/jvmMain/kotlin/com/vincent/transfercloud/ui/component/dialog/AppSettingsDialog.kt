package com.vincent.transfercloud.ui.component.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.theme.LabelLineMedium
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AppSettingsDialog(
	appState: AppState = koinInject<AppState>(),
	viewModel: AppViewModel = koinInject<AppViewModel>(),
	onDismissRequest: () -> Unit
) {
	val scope = rememberCoroutineScope()
	val networkConfig = appState.networkConfig.collectAsState()
	val isConnected = appState.isConnected.collectAsState()
	// --- Mock Data States ---
	// Group 1: General
	var enableNotifications by remember { mutableStateOf(true) }
	// Group 2: Privacy
	var shareLocation by remember { mutableStateOf(false) }
	var allowAnalytics by remember { mutableStateOf(true) }
	// Group 3: System
	var autoUpdate by remember { mutableStateOf(true) }
	var tempHost by remember { mutableStateOf(networkConfig.value.host) }
	var tempPort by remember { mutableStateOf(networkConfig.value.port.toString()) }
	var portError by remember { mutableStateOf(false) }
	// --- UI Dialog ---
	Dialog(onDismissRequest = onDismissRequest) {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			shape = MaterialTheme.shapes.large,
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surface,
			),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
		) {
			Column(
				modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
			) {
				// 1. Dialog Title
				Text(
					text = "Settings",
					style = MaterialTheme.typography.headlineSmall,
					fontWeight = FontWeight.Bold,
					modifier = Modifier.padding(horizontal = 24.dp)
				)

				Spacer(modifier = Modifier.height(16.dp))
				// 2. Scrollable Content
				LazyColumn(
					modifier = Modifier.weight(1f, fill = false)
				) {
					// --- GROUP 1: GENERAL ---
					item {
						SettingsGroupLabel("General")
						SettingsSwitchItem(
							title = "Dark Mode",
							subtitle = "Enable dark theme",
							checked = appState.darkTheme.value,
							onCheckedChange = { appState.darkTheme.value = it }
						)
						SettingsSwitchItem(
							title = "Notifications",
							checked = enableNotifications,
							onCheckedChange = { enableNotifications = it }
						)
						HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
					}

					item {
						val buttons = listOf(
							SettingButton(
								label = "Reset",
								buttonType = "outlined",
								enabled = !isConnected.value,
								onClick = {
									tempHost = networkConfig.value.host
									tempPort = networkConfig.value.port.toString()
									portError = false
								}
							),
							SettingButton(
								label = "Apply",
								buttonType = "elevated",
								enabled = (tempHost != networkConfig.value.host ||
										tempPort.toIntOrNull() != networkConfig.value.port) && !portError,
								onClick = {
									if (!portError) {
										val port = tempPort.toInt()
										if (tempHost != networkConfig.value.host || port != networkConfig.value.port) {
											scope.launch {viewModel.setNetworkConfig(tempHost, port) }
										}
									}
								}
							))
						SettingsGroupLabel("Network Config - ${if (isConnected.value) "Connected" else "Disconnected"}")
						SettingTextFieldItem(
							label = "API Endpoint",
							text = tempHost,
							onTextChange = { tempHost = it },
							modifier = Modifier.fillMaxWidth()
						)
						SettingTextFieldItem(
							label = "Port",
							text = tempPort,
							onTextChange = {
								tempPort = it

								portError = try {
									val port = it.toInt()
									port <= 0 || port > 65535
								} catch (_: NumberFormatException) {

									true
								}
							},
							modifier = Modifier.fillMaxWidth()
						)
						SettingButtonRow(buttons = buttons)
						HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
					}
					// --- GROUP 2: PRIVACY ---
					item {
						SettingsGroupLabel("Privacy")
						SettingsSwitchItem(
							title = "Location Sharing",
							subtitle = "Allow app to access location",
							checked = shareLocation,
							onCheckedChange = { shareLocation = it }
						)
						SettingsSwitchItem(
							title = "Analytics",
							subtitle = "Send anonymous usage data",
							checked = allowAnalytics,
							onCheckedChange = { allowAnalytics = it }
						)
						HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
					}
					// --- GROUP 3: SYSTEM ---
					item {
						SettingsGroupLabel("System")
						SettingsSwitchItem(
							title = "Auto Updates",
							checked = autoUpdate,
							onCheckedChange = { autoUpdate = it }
						)
					}
				}

				Spacer(modifier = Modifier.height(16.dp))
				// 3. Action Button
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp),
					horizontalArrangement = Arrangement.End
				) {
					TextButton(onClick = onDismissRequest) {
						Text("Done")
					}
				}
			}
		}
	}
}

// --- Helper Components ---
@Composable
fun SettingsGroupLabel(text: String) {
	Text(
		text = text,
		style = MaterialTheme.typography.labelLarge,
		color = MaterialTheme.colorScheme.primary,
		fontWeight = FontWeight.Bold,
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 24.dp, vertical = 8.dp)
	)
}

@Composable
fun SettingsSwitchItem(
	title: String,
	subtitle: String? = null,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 24.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = TitleLineLarge.copy(fontWeight = FontWeight.W500, fontSize = 16.sp, lineHeight = 20.sp),
				color = MaterialTheme.colorScheme.onSurface
			)
			if (subtitle != null) {
				Text(
					text = subtitle,
					style = LabelLineMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}

		Spacer(modifier = Modifier.width(16.dp))

		Switch(
			checked = checked,
			onCheckedChange = onCheckedChange
		)
	}
}

@Composable
fun SettingTextFieldItem(
	label: String,
	text: String,
	onTextChange: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.padding(horizontal = 24.dp, vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			style = TitleLineLarge.copy(fontWeight = FontWeight.W500, fontSize = 16.sp, lineHeight = 20.sp),
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.width(120.dp)
		)

		OutlinedTextField(
			value = text,
			label = { Text(label) },
			shape = RoundedCornerShape(8.dp),
			onValueChange = onTextChange,
			singleLine = true,
			modifier = Modifier.weight(1f)
		)
	}
}

@Composable
fun SettingButtonRow(
	buttons: List<SettingButton> = emptyList()
) {
	Row(
		modifier = Modifier.fillMaxWidth()
			.padding(horizontal = 24.dp, vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.End
	) {
		buttons.forEach {
			when (it.buttonType) {
				"outlined" -> {
					OutlinedButton(
						onClick = it.onClick,
						shape = RoundedCornerShape(8.dp),
						modifier = Modifier.padding(horizontal = 8.dp)
					) {
						Text(it.label)
					}
				}

				else -> {
					ElevatedButton(
						onClick = it.onClick,
						shape = RoundedCornerShape(8.dp),
						colors = ButtonDefaults.buttonColors(
							containerColor = MaterialTheme.colorScheme.primary
						),
						modifier = Modifier.padding(horizontal = 8.dp)
					) {
						Text(it.label)
					}
				}
			}
		}
	}

}

data class SettingButton(
	val label: String,
	val buttonType: String,
	val enabled: Boolean = true,
	val onClick: () -> Unit
)