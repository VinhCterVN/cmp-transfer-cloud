package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.FileViewIndex
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import com.vincent.transfercloud.ui.viewModel.FolderObject
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FileChainView(
	appState: AppState = koinInject<AppState>(),
	viewModel: FolderViewModel = koinInject<FolderViewModel>()
) {
	val scope = rememberCoroutineScope()
	val bottomSheetState = LocalBottomSheetScaffoldState.current
	val navigator = LocalNavigator.currentOrThrow
	val chain by appState.breadcrumb.collectAsState()
	val viewIndex by viewModel.currentViewIndex.collectAsState()
	val draggedItem by viewModel.draggedItem.collectAsState()
	val options = listOf(Icons.AutoMirrored.Filled.List, Icons.Default.GridView)

	Row(
		Modifier.fillMaxWidth().padding(4.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		LazyRow(
			Modifier.weight(1f),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start
		) {
			item {
				IconButton({ navigator.pop() }) {
					Icon(Icons.Default.ChevronLeft, null)
				}
			}

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
					Modifier
						.background(if (hovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent, CircleShape)
						.dragAndDropTarget(
							shouldStartDragAndDrop = { event ->
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

		SingleChoiceSegmentedButtonRow {
			options.forEachIndexed { index, icon ->
				SegmentedButton(
					onClick = { viewModel.currentViewIndex.value = if (index > 0) FileViewIndex.GRID else FileViewIndex.LIST },
					selected = viewIndex == if (index > 0) FileViewIndex.GRID else FileViewIndex.LIST,
					label = { Icon(icon, null) },
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
	}
}