package com.vincent.transfercloud.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.zIndex
import com.vincent.transfercloud.ui.component.dialog.AppSettingsDialog
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.theme.HeadLineLarge
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.cloud

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(
	appState: AppState = koinInject<AppState>(),
	viewModel: AppViewModel = koinInject<AppViewModel>()
) {
	val windowState = koinInject<WindowState>()
	val currentUser by appState.currentUser.collectAsState()
	val isDark by appState.darkTheme.collectAsState()
	var expanded by rememberSaveable { mutableStateOf(false) }
	var showSettings by remember { mutableStateOf(false) }
	Row(
		modifier = Modifier.padding(horizontal = 8.dp),
		verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
	) {
		IconButton(
			onClick = {},
		) {
			Icon(Icons.Default.Menu, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
		}
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Image(
				painter = painterResource(Res.drawable.cloud),
				contentDescription = null,
				Modifier.size(45.dp)
			)
			Text(
				"Transfer Cloud",
				style = HeadLineLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
				textAlign = TextAlign.Center,
				maxLines = 1
			)
		}

		Spacer(Modifier.widthIn(4.dp))

		if (windowState.size.width > 600.dp)
			HeadSearchBar()

		Spacer(Modifier.weight(1f))

		TooltipBox(
			state = rememberTooltipState(),
			positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
			tooltip = { PlainTooltip { Text("Theme Switcher", color = MaterialTheme.colorScheme.surface) } },
		) {
			IconButton(onClick = { appState.darkTheme.value = !appState.darkTheme.value }) {
				Icon(
					if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
					null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}

		Box(Modifier.size(50.dp)) {
			TooltipBox(
				state = rememberTooltipState(),
				positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
				tooltip = { PlainTooltip { Text("Theme Switcher", color = MaterialTheme.colorScheme.surface) } },
			) {
				ConnectivityAvatar(
					imageUrl = currentUser!!.avatarUrl!!,
					onClick = { expanded = !expanded }
				)
			}
			DropdownMenu(
				expanded = expanded, { expanded = false },
				offset = DpOffset(x = (-40).dp, y = 0.dp),
				modifier = Modifier.zIndex(10f).widthIn(min = 200.dp),
				containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
				shape = RoundedCornerShape(12.dp)
			) {
				DropdownMenuItem(
					text = { Text(currentUser?.fullName ?: "Guest", color = MaterialTheme.colorScheme.onSurfaceVariant) },
					leadingIcon = { Icon(Icons.Default.Person, null) },
					onClick = {},
					modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
				)
				DropdownMenuItem(
					text = { Text("Settings", color = MaterialTheme.colorScheme.onSurfaceVariant) },
					leadingIcon = { Icon(Icons.Default.Settings, null) },
					onClick = { showSettings = true },
					modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
				)
				HorizontalDivider()
				DropdownMenuItem(
					text = { Text("Logout", color = MaterialTheme.colorScheme.onSurfaceVariant) },
					leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
					onClick = { viewModel.logout() },
					modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
				)
			}
		}
	}
	if (showSettings)
		AppSettingsDialog { showSettings = false }
}