package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.FileViewIndex
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import com.vincent.transfercloud.ui.viewModel.FolderObject
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.ui.viewModel.ShareViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FileChainView(
	appState: AppState = koinInject<AppState>(),
	viewModel: FolderViewModel = koinInject<FolderViewModel>(),
	shareVM: ShareViewModel = koinViewModel()
) {
	val scope = rememberCoroutineScope()
	val bottomSheetState = LocalBottomSheetScaffoldState.current
	val navigator = LocalNavigator.currentOrThrow
	val tab by appState.currentTab.collectAsState()
	val chain by appState.breadcrumb.collectAsState()
	val viewIndex by viewModel.currentViewIndex.collectAsState()
	val draggedItem by viewModel.draggedItem.collectAsState()
	var isControlHovered by remember { mutableStateOf(false) }
	val options = listOf(
		FolderViewOption(
			icon = Icons.AutoMirrored.Outlined.List,
			selectedIcon = Icons.AutoMirrored.Filled.List,
			selected = { viewIndex == FileViewIndex.LIST },
			onClick = { viewModel.currentViewIndex.value = FileViewIndex.LIST }
		),
		FolderViewOption(
			icon = Icons.Outlined.GridView,
			selectedIcon = Icons.Filled.GridView,
			selected = { viewIndex == FileViewIndex.GRID },
			onClick = { viewModel.currentViewIndex.value = FileViewIndex.GRID }
		),
	)

	Row(
		Modifier
			.fillMaxWidth()
			.padding(4.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		LazyRow(
			Modifier.weight(1f),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start
		) {
			if (tab == AppState.AppTab.MY_DRIVE)
				item {
					IconButton({ navigator.pop() }) {
						Icon(Icons.Default.ChevronLeft, null)
					}
				}

			when (tab) {
				AppState.AppTab.MY_DRIVE -> {
					itemsIndexed(chain) { index, folder ->
						var hovered by remember { mutableStateOf(false) }
						val dragAndDropTarget = remember(folder.id, viewModel) {
							object : DragAndDropTarget {
								override fun onEntered(event: DragAndDropEvent) {
									hovered = true
								}

								override fun onEnded(event: DragAndDropEvent) {
									hovered = false
								}

								override fun onExited(event: DragAndDropEvent) {
									hovered = false
								}

								override fun onDrop(event: DragAndDropEvent): Boolean {
									viewModel.setHoveredFolder(null)
									viewModel.draggedItem.value?.let {
										scope.launch {
											bottomSheetState.snackbarHostState.showSnackbar(
												"${if (it.second == FolderObject.FOLDER) "Folder" else "File"} has been moved to ${folder.name}.",
												actionLabel = "OK",
												duration = SnackbarDuration.Short,
											)
										}
										viewModel.moveItem(folder.id)
										return true
									}
									return false
								}
							}
						}

						Box(
							Modifier.background(
								if (hovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
								CircleShape
							)
								.dragAndDropTarget(
									shouldStartDragAndDrop = { _ ->
										draggedItem != null && draggedItem?.first != folder.id
									},
									target = dragAndDropTarget
								)
						) {
							TextButton(
								onClick = { if (index != chain.size - 1) navigator.push(FolderDetailView(folder.id)) else null },
								contentPadding = ButtonDefaults.SmallContentPadding,
								interactionSource = null,
								modifier = Modifier.padding(0.dp)
							) {
								Text(
									folder.name,
									style = TitleLineLarge.copy(
										fontWeight = FontWeight.W600
									),
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
						}
						if (index < chain.size - 1) {
							Icon(Icons.Default.ChevronRight, null)
						}
					}
				}

				AppState.AppTab.SHARED -> {
					item {
						TextButton(
							onClick = { shareVM.getSharedData() },
							contentPadding = ButtonDefaults.SmallContentPadding,
							interactionSource = null,
							modifier = Modifier.padding(0.dp)
						) {
							Text(
								"Shared with me",
								style = TitleLineLarge.copy(
									fontSize = 20.sp,
									fontWeight = FontWeight.W600
								),
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}

				else -> {}
			}
		}


		Row(
			modifier = Modifier
				.onPointerEvent(PointerEventType.Enter) { isControlHovered = true }
				.onPointerEvent(PointerEventType.Exit) { isControlHovered = false },
			verticalAlignment = Alignment.CenterVertically
		) {
			SingleChoiceSegmentedButtonRow {
				options.forEachIndexed { index, icon ->
					SegmentedButton(
						onClick = icon.onClick,
						selected = icon.selected(),
						label = { Icon(if (icon.selected()) icon.selectedIcon else icon.icon, null) },
						colors = SegmentedButtonDefaults.colors(
							activeContainerColor = MaterialTheme.colorScheme.primaryContainer
						),
						shape = SegmentedButtonDefaults.itemShape(
							index = index,
							count = options.size
						),
					)
				}
			}
			AnimatedVisibility(
				visible = isControlHovered,
				enter = expandHorizontally(
					animationSpec = tween(durationMillis = 300, easing = EaseOut),
					expandFrom = Alignment.End
				) + fadeIn(tween(300)),
				exit = shrinkHorizontally(
					animationSpec = tween(durationMillis = 300, easing = EaseOut),
					shrinkTowards = Alignment.End
				) + fadeOut(tween(300))
			) {
				IconButton(onClick = { appState.fileDetailShow.value = !appState.fileDetailShow.value }) {
					val icon = if (appState.fileDetailShow.value)
						Icons.AutoMirrored.Filled.ArrowForward
					else
						Icons.AutoMirrored.Filled.ArrowBack

					Icon(icon, contentDescription = "Toggle Details")
				}
			}
		}
	}
}

data class FolderViewOption(
	val icon: ImageVector,
	val selectedIcon: ImageVector,
	val selected: () -> Boolean = { false },
	val onClick: () -> Unit
)