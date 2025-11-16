package com.vincent.transfercloud.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import com.vincent.transfercloud.ui.theme.HeadLineLarge
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.cloud
import transfercloud.composeapp.generated.resources.mail

@Composable
fun HeaderBar(
	appState: AppState = koinInject<AppState>(),
	viewModel: AppViewModel = koinInject<AppViewModel>()
) {
	val windowState = koinInject<WindowState>()
	val currentUser by appState.currentUser.collectAsState()
	var expanded by rememberSaveable { mutableStateOf(false) }

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
				textAlign = TextAlign.Center
			)
		}

		Spacer(Modifier.widthIn(4.dp))

		if (windowState.size.width > 600.dp)
			HeadSearchBar()

		Spacer(Modifier.weight(1f))

		Box(Modifier.size(50.dp)) {
			AsyncImage(
				model = "https://i.pravatar.cc/150?u=${currentUser?.name ?: "James"}",
				contentDescription = "Avatar",
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize()
					.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
					.padding(3.dp).clip(CircleShape).aspectRatio(1f).clickable(
						onClick = { expanded = !expanded }
					)
			)
			DropdownMenu(
				expanded, { expanded = false },
				offset = DpOffset(x = (-40).dp, y = 0.dp),
				modifier = Modifier.zIndex(10f).widthIn(min = 200.dp),
				containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
				shape = RoundedCornerShape(12.dp)
			) {
				DropdownMenuItem(
					text = { Text(currentUser?.name ?: "Guest") },
					leadingIcon = { Icon(Icons.Default.Person, null) },
					onClick = {}
				)
				DropdownMenuItem(
					text = { Text("Settings") },
					leadingIcon = { Icon(Icons.Default.Settings, null) },
					onClick = {}
				)
				HorizontalDivider()
				DropdownMenuItem(
					text = { Text("Logout") },
					leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
					onClick = { viewModel.logout() }
				)
			}
		}
	}

}