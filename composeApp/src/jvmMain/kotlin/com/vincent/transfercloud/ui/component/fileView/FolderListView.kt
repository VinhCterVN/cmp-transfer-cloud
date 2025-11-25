package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferAction
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.component.dialog.FileOptionMenu
import com.vincent.transfercloud.ui.component.dialog.formatFileSize
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.state.getFileIcon
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.viewModel.FolderObject
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.utils.formatIsoToMonthDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.empty_state_empty_folder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.FolderListView(
	listState: LazyListState,
	viewModel: FolderViewModel = koinInject<FolderViewModel>()
) {
	val scope = rememberCoroutineScope()
	val windowInfo = LocalWindowInfo.current
	val navigator = LocalNavigator.currentOrThrow
	val folderData by viewModel.folderData.collectAsState()
	val selectedIds by viewModel.selectedIds.collectAsState()
	val draggedItem by viewModel.draggedItem.collectAsState()
	val hoveredFolderId by viewModel.hoveredFolderId.collectAsState()
	val bottomSheetState = LocalBottomSheetScaffoldState.current
	val foldersExpanded = remember { mutableStateOf(true) }
	val filesExpanded = remember { mutableStateOf(true) }
	var openMenuFolderId by remember { mutableStateOf<String?>(null) }
	var lastSelectedIndex by remember { mutableStateOf(-1) }
	val borderColor = MaterialTheme.colorScheme.primary
	val tableHeadStyle = TextStyle(
		fontWeight = FontWeight.SemiBold,
		fontSize = 14.sp,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
	val tableRowStyle = TextStyle(
		fontSize = 14.sp,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
	val tableHeads = listOf(
		TableHead("Name", 1f),
		TableHead("Owner", 0.35f),
		TableHead("Updated", 0.3f),
		TableHead("Size", 0.25f),
		TableHead("Type", 0.3f)
	)
	val fileTableHeads = listOf(
		TableHead("Name", 1f),
		TableHead("Owner", 0.35f),
		TableHead("Modified", 0.3f),
		TableHead("Size", 0.25f),
		TableHead("Type", 0.3f)
	)

	fun handleSelection(index: Int, id: String, modifiers: PointerKeyboardModifiers) {
		val isCtrl = modifiers.isCtrlPressed || modifiers.isMetaPressed
		val isShift = modifiers.isShiftPressed

		when {
			isShift && lastSelectedIndex != -1 -> {
				val start = minOf(lastSelectedIndex, index)
				val end = maxOf(lastSelectedIndex, index)
				val rangeIds = folderData?.subfolders?.subList(start, end + 1)?.map { it.id }
				viewModel.setSelectedIds(rangeIds?.toSet() ?: emptySet())
			}

			isCtrl -> {
				viewModel.setSelectedIds(if (selectedIds.contains(id)) selectedIds - id else selectedIds + id)
				lastSelectedIndex = index
			}

			else -> {
				viewModel.setSelectedIds(setOf(id))
				lastSelectedIndex = index
			}
		}
	}
	if (folderData?.subfolders.isNullOrEmpty() && folderData?.files.isNullOrEmpty()) {
		Column(
			modifier = Modifier.fillMaxHeight().align(Alignment.CenterHorizontally),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Image(
				painter = painterResource(Res.drawable.empty_state_empty_folder),
				contentDescription = null,
				modifier = Modifier.size(250.dp)
			)
			Spacer(Modifier.height(8.dp))
			Text("Drop files to upload", style = TitleLineBig.copy(fontSize = 20.sp))
		}
	} else {
		LazyColumn(
			state = listState,
			contentPadding = PaddingValues(8.dp),
		) {
			folderViewSticky(
				showSticky = selectedIds.isNotEmpty(),
				count = selectedIds.size,
				onClear = {}
			)
			if (!folderData?.subfolders.isNullOrEmpty()) {
				item {
					Row(
						modifier = Modifier
							.padding(horizontal = 8.dp, vertical = 12.dp)
							.clip(RoundedCornerShape(8.dp))
							.clickable { foldersExpanded.value = !foldersExpanded.value }
							.padding(horizontal = 8.dp, vertical = 4.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						Icon(
							if (foldersExpanded.value) Icons.Default.KeyboardArrowDown
							else Icons.AutoMirrored.Filled.KeyboardArrowRight,
							contentDescription = null,
							modifier = Modifier.size(24.dp)
						)
						Text(
							"Folders (${folderData?.subfolders?.size ?: 0})",
							style = TextStyle(
								fontWeight = FontWeight.Bold,
								fontSize = 16.sp
							)
						)
					}
				}
				// Table Header
				if (foldersExpanded.value) {
					item {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
								.padding(horizontal = 16.dp, vertical = 8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							tableHeads.map { head ->
								Text(
									head.name,
									style = tableHeadStyle,
									modifier = Modifier.weight(head.weight)
								)
							}
							Spacer(Modifier.width(40.dp)) // Space for menu button
						}
					}
					// Folders list items
					itemsIndexed(
						items = folderData?.subfolders ?: emptyList(),
						key = { _, folder -> folder.id }
					) { index, folder ->
						val isLast = index == (folderData?.subfolders?.lastIndex ?: -1)
						val color = MaterialTheme.colorScheme.outlineVariant
						var hasAppeared by rememberSaveable(folder.id) { mutableStateOf(false) }
						val isHovered = hoveredFolderId == folder.id
						val isSelected = selectedIds.contains(folder.id)
						val animatedProgress = remember(folder.id) {
							Animatable(initialValue = if (hasAppeared) 1f else 0f)
						}
						val dragAndDropTarget = remember(folder.id, viewModel) {
							object : DragAndDropTarget {
								override fun onEntered(event: DragAndDropEvent) {
									viewModel.setHoveredFolder(folder.id)
								}

								override fun onExited(event: DragAndDropEvent) {
									if (viewModel.hoveredFolderId.value == folder.id) {
										viewModel.setHoveredFolder(null)
									}
								}

								override fun onDrop(event: DragAndDropEvent): Boolean {
									viewModel.setHoveredFolder(null)
									viewModel.draggedItem.value?.let {
										scope.launch { bottomSheetState.snackbarHostState.showSnackbar(
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

						LaunchedEffect(folder.id) {
							if (!hasAppeared) {
								delay((index % 10) * 50L)
								animatedProgress.animateTo(
									targetValue = 1f,
									animationSpec = tween(300)
								)
								hasAppeared = true
							}
						}
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.background(
									if (isSelected) MaterialTheme.colorScheme.surfaceVariant
									else if (index % 2 == 0) Color.Transparent
									else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
								)
								.drawBehind {
									if (!isLast) {
										val stroke = 1.dp.toPx()
										drawLine(
											color = color,
											start = Offset(0f, size.height),
											end = Offset(size.width, size.height),
											strokeWidth = stroke
										)
									}
								}
								.drawWithContent {
									drawContent()
									if (isHovered) {
										drawRoundRect(
											color = borderColor,
											size = size,
											cornerRadius = CornerRadius(12.dp.toPx()),
											style = Stroke(width = 2.dp.toPx())
										)
									}
								}
								.combinedClickable(
									onClick = {
										val modifiers = windowInfo.keyboardModifiers
										handleSelection(index, folder.id, modifiers)
									},
									onDoubleClick = {
										scope.launch { navigator.push(FolderDetailView(folder.id)) }
									},
								)
								.graphicsLayer {
									alpha = animatedProgress.value
									val scale = 0.8f + (0.2f * animatedProgress.value)
									scaleX = scale
									scaleY = scale
								}
								.dragAndDropSource { offset ->
									viewModel.startDragging(folder.id to FolderObject.FOLDER)
									DragAndDropTransferData(
										transferable = createTransferable(folder.id),
										dragDecorationOffset = Offset.Zero,
										supportedActions = listOf(
											DragAndDropTransferAction.Move,
											DragAndDropTransferAction.Copy,
										),
										onTransferCompleted = { action ->
											viewModel.stopDragging()
										},
									)
								}
								.dragAndDropTarget(
									shouldStartDragAndDrop = { event ->
										draggedItem != null && draggedItem?.first != folder.id
									},
									target = dragAndDropTarget
								)
								.padding(horizontal = 16.dp, vertical = 8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							// Name column
							Row(
								modifier = Modifier.weight(1f),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(12.dp)
							) {
								Icon(
									Icons.Default.Folder,
									null,
									tint = MaterialTheme.colorScheme.primary,
									modifier = Modifier.size(24.dp)
								)
								Text(
									folder.name,
									style = TextStyle(
										fontWeight = FontWeight.Medium,
										fontSize = 15.sp
									),
									maxLines = 1,
									overflow = TextOverflow.Ellipsis
								)
							}
							Row(
								modifier = Modifier.weight(0.35f),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(4.dp)
							) {
								AsyncImage(
									model = "https://i.pravatar.cc/150?u=Me",
									contentDescription = null,
									contentScale = ContentScale.Crop,
									modifier = Modifier.size(28.dp).clip(CircleShape)
								)
								Text(
									"Me",
									style = tableRowStyle,
								)
							}
							// Created column
							Text(
								formatIsoToMonthDay(folder.updatedAt),
								style = tableRowStyle,
								modifier = Modifier.weight(0.3f)
							)
							// Size column
							Text(
								"—",
								style = tableRowStyle,
								modifier = Modifier.weight(0.25f)
							)
							// Type column
							Text(
								"Folder",
								style = tableRowStyle,
								modifier = Modifier.weight(0.3f)
							)
							// Menu button
							Box {
								Box(
									modifier = Modifier
										.clip(CircleShape)
										.pointerHoverIcon(PointerIcon.Hand)
										.clickable(onClick = { openMenuFolderId = folder.id })
										.padding(8.dp)
								) {
									Icon(
										Icons.Default.MoreVert,
										null,
										tint = MaterialTheme.colorScheme.onSurfaceVariant,
										modifier = Modifier.size(18.dp)
									)
								}
								FileOptionMenu(
									expanded = openMenuFolderId == folder.id,
									onDismissRequest = { openMenuFolderId = null },
									onRename = { openMenuFolderId = null },
									onMove = { openMenuFolderId = null }, onShare = { openMenuFolderId = null },
									onDownload = {},
									onDelete = {
										scope.launch {
											openMenuFolderId = null
											val res = bottomSheetState.snackbarHostState.showSnackbar(
												"Folder ${folder.name} has been deleted.",
												actionLabel = "Undo",
												duration = SnackbarDuration.Short,
											)
											if (res == SnackbarResult.Dismissed)
												viewModel.deleteFolder(folder.id, folder.ownerId)
										}
									}
								)
							}
						}
					}
				}
			}
			// Header cho Files
			if (!folderData?.files.isNullOrEmpty()) {
				item {
					Row(
						modifier = Modifier
							.padding(horizontal = 8.dp, vertical = 12.dp)
							.clip(RoundedCornerShape(8.dp))
							.clickable { filesExpanded.value = !filesExpanded.value }
							.padding(horizontal = 8.dp, vertical = 4.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						Icon(
							if (filesExpanded.value) Icons.Default.KeyboardArrowDown
							else Icons.AutoMirrored.Filled.KeyboardArrowRight,
							contentDescription = null,
							modifier = Modifier.size(24.dp)
						)
						Text(
							"Files (${folderData?.files?.size ?: 0})",
							style = TextStyle(
								fontWeight = FontWeight.Bold,
								fontSize = 16.sp
							)
						)
					}
				}
				if (filesExpanded.value) {
					item {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
								.padding(horizontal = 16.dp, vertical = 8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							fileTableHeads.map { head ->
								Text(
									head.name,
									style = tableHeadStyle,
									modifier = Modifier.weight(head.weight)
								)
							}
							Spacer(Modifier.width(40.dp))
						}
					}
					itemsIndexed(
						items = folderData?.files ?: emptyList(),
						key = { _, file -> file.id }
					) { index, file ->
						val isLast = index == (folderData?.subfolders?.lastIndex ?: -1)
						val color = MaterialTheme.colorScheme.outlineVariant
						val isSelected = selectedIds.contains(file.id)
						var hasAppeared by rememberSaveable(file.id) { mutableStateOf(false) }
						val animatedProgress = remember(file.id) {
							Animatable(initialValue = if (hasAppeared) 1f else 0f)
						}
						LaunchedEffect(file.id) {
							if (!hasAppeared) {
								delay((index % 10) * 50L)
								animatedProgress.animateTo(
									targetValue = 1f,
									animationSpec = tween(300)
								)
								hasAppeared = true
							}
						}

						Row(
							modifier = Modifier
								.fillMaxWidth()
//								.clip(RoundedCornerShape(8.dp))
								.combinedClickable(
									onClick = {
										val modifiers = windowInfo.keyboardModifiers
										handleSelection(index, file.id, modifiers)
									},
									onDoubleClick = {/*TODO: IMPLEMENT FILE PREVIEW*/}
								)
								.background(
									if (isSelected) MaterialTheme.colorScheme.surfaceVariant
									else if (index % 2 == 0) Color.Transparent
									else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
								)
								.drawBehind {
									if (!isLast) {
										val stroke = 1.dp.toPx()
										drawLine(
											color = color,
											start = Offset(0f, size.height),
											end = Offset(size.width, size.height),
											strokeWidth = stroke
										)
									}
								}
								.graphicsLayer {
									alpha = animatedProgress.value
									val scale = 0.8f + (0.2f * animatedProgress.value)
									scaleX = scale
									scaleY = scale
								}
								.dragAndDropSource { offset ->
									viewModel.startDragging(file.id to FolderObject.FILE)
									DragAndDropTransferData(
										transferable = createTransferable(file.id),
										dragDecorationOffset = Offset.Zero,
										supportedActions = listOf(
											DragAndDropTransferAction.Move,
											DragAndDropTransferAction.Copy,
										),
										onTransferCompleted = { action ->
											viewModel.stopDragging()
										},
									)
								}
								.padding(horizontal = 16.dp, vertical = 8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Row(
								modifier = Modifier.weight(1f),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(12.dp)
							) {
								Icon(
									painterResource(getFileIcon(file.name)),
									null,
									tint = Color.Unspecified,
									modifier = Modifier.size(24.dp)
								)
								Text(
									file.name,
									style = TextStyle(
										fontWeight = FontWeight.Medium,
										fontSize = 15.sp
									),
									maxLines = 1,
									overflow = TextOverflow.Ellipsis
								)
							}
							// Owner column
							Row(
								modifier = Modifier.weight(0.35f),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(4.dp)
							) {
								AsyncImage(
									model = "https://i.pravatar.cc/150?u=User$index",
									contentDescription = null,
									contentScale = ContentScale.Crop,
									modifier = Modifier.size(28.dp).clip(CircleShape)
								)
								Text(
									"User $index".lowercase(),
									style = tableRowStyle,
								)
							}
							// Modified column
							Text(
								formatIsoToMonthDay(file.updatedAt),
								style = tableRowStyle,
								modifier = Modifier.weight(0.3f)
							)

							Text(
								formatFileSize(file.fileSize),
								style = tableRowStyle,
								modifier = Modifier.weight(0.25f)
							)

							Text(
								file.mimeType,
								style = tableRowStyle,
								modifier = Modifier.weight(0.3f),
								maxLines = 1
							)

							Box {
								Box(
									modifier = Modifier
										.clip(CircleShape)
										.pointerHoverIcon(PointerIcon.Hand)
										.clickable { openMenuFolderId = file.id }
										.padding(8.dp)
								) {
									Icon(
										Icons.Default.MoreVert,
										null,
										tint = MaterialTheme.colorScheme.onSurfaceVariant,
										modifier = Modifier.size(18.dp)
									)
								}
								FileOptionMenu(
									expanded = openMenuFolderId == file.id,
									onDismissRequest = { openMenuFolderId = null },
									onRename = { openMenuFolderId = null },
									onMove = { openMenuFolderId = null }, onShare = { openMenuFolderId = null },
									onDownload = { viewModel.downloadFile(file) },
									onDelete = {
										scope.launch {
											openMenuFolderId = null
											val res = bottomSheetState.snackbarHostState.showSnackbar(
												"File ${file.name} has been deleted.",
												actionLabel = "Undo",
												duration = SnackbarDuration.Short,
											)
											if (res == SnackbarResult.Dismissed)
												viewModel.deleteFile(file.id, file.ownerId)
										}
									}
								)
							}
						}
					}
				}
			}

			item {
				Spacer(Modifier.height(80.dp))
			}
		}
	}
}

data class TableHead(
	val name: String,
	val weight: Float
)