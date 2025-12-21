package com.vincent.transfercloud.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.theme.LabelLineSmall
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SideBar(
	appState: AppState = koinInject<AppState>()
) {
	val currentTab by appState.currentTab.collectAsState()
	val sideBarItems = listOf(
		SideBarOption(
			"Home",
			selectedIcon = Icons.Rounded.Home,
			unselectedIcon = Icons.Outlined.Home,
			tab = AppState.AppTab.HOME,
			onClick = { tab -> appState.currentTab.value = tab }
		),
		SideBarOption(
			"My Drive",
			selectedIcon = Icons.Rounded.FolderOpen,
			unselectedIcon = Icons.Outlined.Folder,
			tab = AppState.AppTab.MY_DRIVE,
			onClick = { tab -> appState.currentTab.value = tab }),
		SideBarOption(
			"Shared with me",
			selectedIcon = Icons.Rounded.Groups,
			unselectedIcon = Icons.Outlined.Groups,
			tab = AppState.AppTab.SHARED,
			onClick = { tab -> appState.currentTab.value = tab }),
		SideBarOption(
			"Direct Transfer",
			selectedIcon = Icons.AutoMirrored.Filled.CompareArrows,
			unselectedIcon = Icons.Default.SwapHoriz,
			tab = AppState.AppTab.TRANSFER,
			onClick = { tab -> appState.currentTab.value = tab }
		),
		SideBarOption(
			"Recycle Bin",
			selectedIcon = Icons.Filled.RestoreFromTrash,
			unselectedIcon = Icons.Outlined.RestoreFromTrash,
			tab = AppState.AppTab.TRASH,
			onClick = { tab -> appState.currentTab.value = tab }
		),
	)

	Column(
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		ExtendedFloatingActionButton(
			onClick = { if (currentTab == AppState.AppTab.MY_DRIVE) appState.isCreatingFolder.value = true },
			icon = { Icon(Icons.Filled.Add, null) },
			text = { Text("New") }
		)
		Spacer(Modifier.height(8.dp))
		sideBarItems.forEachIndexed { index, item ->
			val isSelected = currentTab == item.tab
			val iconColor by animateColorAsState(
				targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
				animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
				label = "IconColor"
			)
			val iconProgress by animateFloatAsState(
				targetValue = if (isSelected) 1f else 0f,
				animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
				label = "IconProgress"
			)

			NavigationDrawerItem(
				icon = {
					Icon(
						if (isSelected) item.selectedIcon else item.unselectedIcon,
						contentDescription = null,
						modifier = Modifier.animateIcon(
							checkedProgress = { iconProgress },
							color = { iconColor },
							size = { max(28.dp * it, 24.dp) }
						)
					)
				},
				label = {
					Text(item.name, style = LabelLineSmall.copy(fontSize = 15.sp))
				},
				selected = isSelected,
				onClick = { item.onClick(item.tab) },
				shape = CircleShape,
				colors = NavigationDrawerItemDefaults.colors(
					selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.75f)
				),
				modifier = Modifier.height(35.dp)
			)
		}
	}
}

data class SideBarOption(
	val name: String,
	val selectedIcon: ImageVector,
	val unselectedIcon: ImageVector,
	val tab: AppState.AppTab,
	val onClick: (AppState.AppTab) -> Unit
)