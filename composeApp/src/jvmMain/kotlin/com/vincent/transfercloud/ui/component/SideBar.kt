package com.vincent.transfercloud.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.EmailIndex
import com.vincent.transfercloud.ui.theme.LabelLineSmall
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import org.koin.compose.koinInject

@Composable
fun SideBar(
	viewModel: AppViewModel = koinInject<AppViewModel>(),
	appState: AppState = koinInject<AppState>()
) {

	val currentIndex = appState.currentIndex
	var selectedIndex by remember { mutableStateOf(0) }

	val sideBarItems = listOf(
		SideBarOption(
			"Home",
			selectedIcon = Icons.Rounded.Home,
			unselectedIcon = Icons.Outlined.Home,
			onClick = { selectedIndex = 0; currentIndex.value = EmailIndex.INBOX }
		),
		SideBarOption(
			"My Drive",
			selectedIcon = Icons.Rounded.FolderOpen,
			unselectedIcon = Icons.Outlined.Folder,
			onClick = { selectedIndex = 1; currentIndex.value = EmailIndex.SENT }
		),
		SideBarOption(
			"Shared with me",
			selectedIcon = Icons.Rounded.Groups,
			unselectedIcon = Icons.Outlined.Groups,
			onClick = { selectedIndex = 2; currentIndex.value = EmailIndex.DRAFTS }
		)
	)

	Column(
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		ExtendedFloatingActionButton(
			onClick = {
				appState.isComposing.value = true
			},
			icon = { Icon(Icons.Filled.Edit, null) },
			text = { Text("Compose") }
		)
		Spacer(Modifier.height(8.dp))
		sideBarItems.forEachIndexed { index, item ->
			NavigationDrawerItem(
				icon = { Icon(if (selectedIndex == index) item.selectedIcon else item.unselectedIcon, null) },
				label = { Text(item.name, style = LabelLineSmall.copy(
					fontSize = 14.sp
				)) },
				selected = selectedIndex == index,
				onClick = item.onClick,
				shape = CircleShape,
				colors = NavigationDrawerItemDefaults.colors(
					selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.75f)
				),
				modifier = Modifier.height(40.dp)
			)
		}
	}
}

data class SideBarOption(
	val name: String,
	val selectedIcon: ImageVector,
	val unselectedIcon: ImageVector,
	val onClick: () -> Unit
)