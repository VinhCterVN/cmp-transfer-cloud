@file:OptIn(ExperimentalComposeUiApi::class)

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
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
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
import com.vincent.transfercloud.ui.component.button.ExpandButton
import com.vincent.transfercloud.ui.component.dialog.FileOptionMenu
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.state.getFileIcon
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.viewModel.FolderObject
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.utils.cursorHand
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.empty_state_empty_folder
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.FolderGridView(
	listState: LazyGridState,
	viewModel: FolderViewModel = koinInject<FolderViewModel>()
) {
	val scope = rememberCoroutineScope()
	val windowInfo = LocalWindowInfo.current
	val navigator = LocalNavigator.currentOrThrow
	val bottomSheetState = LocalBottomSheetScaffoldState.current
	val folderData by viewModel.folderData.collectAsState()
	val tempFiles by viewModel.tempFiles.collectAsState()
	val foldersExpanded = remember { mutableStateOf(true) }
	val filesExpanded = remember { mutableStateOf(true) }
	var openMenuFolderId by remember { mutableStateOf<String?>(null) }
	val selectedIds by viewModel.selectedIds.collectAsState()
	val draggedItem by viewModel.draggedItem.collectAsState()
	val hoveredFolderId by viewModel.hoveredFolderId.collectAsState()
	val borderColor = MaterialTheme.colorScheme.primary

	LaunchedEffect(draggedItem) {
		viewModel.setHoveredFolder(null)
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
		LazyVerticalGrid(
			state = listState,
			columns = GridCells.Adaptive(minSize = 250.dp),
			contentPadding = PaddingValues(8.dp),
		) {
			folderViewSticky()
			if (!folderData?.subfolders.isNullOrEmpty()) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ExpandButton(
						foldersExpanded,
						"Folders (${folderData?.subfolders?.size ?: 0})"
					)
				}
				if (foldersExpanded.value) {
					itemsIndexed(
						items = folderData?.subfolders ?: emptyList(),
						key = { _, folder -> folder.id }
					) { index, folder ->
						val isSelected = remember(selectedIds) { selectedIds.contains(folder.id) }
						var hasAppeared by rememberSaveable(folder.id) { mutableStateOf(false) }
						val isHovered = hoveredFolderId == folder.id
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
										viewModel.moveItem(folder.id)
										scope.launch {
											bottomSheetState.snackbarHostState.showSnackbar(
												"${if (it.second == FolderObject.FOLDER) "Folder" else "File"} has been moved to ${folder.name}.",
												actionLabel = "OK",
												duration = SnackbarDuration.Short,
											)
										}
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
						Card(
							shape = RoundedCornerShape(12.dp),
							elevation = CardDefaults.cardElevation(2.dp),
							colors = CardDefaults.cardColors(
								containerColor = when {
									isHovered || isSelected -> MaterialTheme.colorScheme.primaryContainer
									else -> MaterialTheme.colorScheme.surfaceVariant
								}
							),
							modifier = Modifier
								.padding(8.dp).height(55.dp)
								.clip(RoundedCornerShape(12.dp)).cursorHand()
								.graphicsLayer {
									alpha = animatedProgress.value
									val scale = 0.8f + (0.2f * animatedProgress.value)
									scaleX = scale
									scaleY = scale
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
								.dragAndDropSource { offset ->
									viewModel.startDragging(folder.id to FolderObject.FOLDER)
									DragAndDropTransferData(
										transferable = createTransferable(folder.id),
										dragDecorationOffset = Offset.Zero,
										supportedActions = listOf(
											DragAndDropTransferAction.Move,
											DragAndDropTransferAction.Copy,
										),
										onTransferCompleted = { viewModel.stopDragging() },
									)
								}
								.dragAndDropTarget(
									shouldStartDragAndDrop = { event ->
										draggedItem != null && draggedItem?.first != folder.id
									},
									target = dragAndDropTarget
								).combinedClickable(
									onClick = {
										val modifiers = windowInfo.keyboardModifiers
										val isCtrlPressed = modifiers.isCtrlPressed || modifiers.isMetaPressed
										viewModel.toggleSelection(folder.id, isCtrlPressed)
									},
									onDoubleClick = { navigator.push(FolderDetailView(folder.id)) },
								),
						) {
							Row(
								Modifier.fillMaxSize().padding(horizontal = 12.dp),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(12.dp)
							) {
								Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
								Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
									Text(
										folder.name,
										style = TextStyle(
											fontWeight = FontWeight.SemiBold,
											fontSize = 16.sp
										)
									)
//									Text(
//										folder.createdAt,
//										style = LabelLineSmall.copy(fontWeight = FontWeight.Normal, fontSize = 12.sp)
//									)
								}
								Spacer(Modifier.weight(1f))
								Box {
									Box(
										modifier = Modifier.clip(CircleShape).cursorHand()
											.clickable(
												onClick = { openMenuFolderId = folder.id },
											)
											.padding(4.dp)
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
										onMove = { openMenuFolderId = null },
										onShare = { openMenuFolderId = null; viewModel.emitSharingItem(folder.id, true) },
										onDownload = {
											scope.launch {
												bottomSheetState.snackbarHostState.showSnackbar(
													"Downloading ${folder.name}...",
													actionLabel = "OK",
													duration = SnackbarDuration.Short
												)
												viewModel.downloadFolder(folder)
												openMenuFolderId = null
											}
										},
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
			}
			if (!folderData?.files.isNullOrEmpty()) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ExpandButton(filesExpanded, "Files (${folderData?.files?.size ?: 0})")
				}
				if (filesExpanded.value) {
					itemsIndexed(
						items = folderData?.files ?: emptyList(),
						key = { _, file -> file.id }
					) { index, file ->
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

						Card(
							elevation = CardDefaults.cardElevation(2.dp),
							shape = RoundedCornerShape(12.dp),
							colors = CardDefaults.cardColors(
								containerColor = when {
									isSelected -> MaterialTheme.colorScheme.primaryContainer
									else -> MaterialTheme.colorScheme.surfaceVariant
								}
							),
							modifier = Modifier.padding(8.dp).aspectRatio(1f)
								.clip(RoundedCornerShape(12.dp)).cursorHand()
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
								.combinedClickable(
									onClick = {
										val modifiers = windowInfo.keyboardModifiers
										val isCtrlPressed = modifiers.isCtrlPressed || modifiers.isMetaPressed
										viewModel.toggleSelection(file.id, isCtrlPressed)
									},
									onDoubleClick = {}
								)
						) {
							Column(
								Modifier.fillMaxSize().padding(8.dp)
							) {
								Row(
									modifier = Modifier.padding(4.dp).fillMaxWidth(),
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.spacedBy(12.dp)
								) {
									Icon(
										painterResource(getFileIcon(file.name)), null, tint = Color.Unspecified
									)
									Text(
										file.name,
										style = TextStyle(
											fontWeight = FontWeight.SemiBold,
											fontSize = 16.sp
										),
										maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
									)
									Box {
										Box(
											modifier = Modifier
												.clip(CircleShape)
												.pointerHoverIcon(PointerIcon.Hand)
												.clickable(
													onClick = { openMenuFolderId = file.id },
												)
												.padding(4.dp)
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
											onMove = { openMenuFolderId = null },
											onShare = { openMenuFolderId = null; viewModel.emitSharingItem(file.id, false) },
											onDownload = {
												scope.launch {
													bottomSheetState.snackbarHostState.showSnackbar(
														"Downloading ${file.name}...",
														actionLabel = "OK",
														duration = SnackbarDuration.Short
													)
													viewModel.downloadFile(file).join()
													openMenuFolderId = null
													bottomSheetState.snackbarHostState.showSnackbar(
														"${file.name} has been downloaded.",
														actionLabel = "OK",
														duration = SnackbarDuration.Short
													)
												}
											},
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

								Column(
									Modifier.weight(1f).padding(vertical = 8.dp)
								) {
									Box(
										Modifier.fillMaxSize()
											.clip(RoundedCornerShape(4.dp))
											.background(MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), RoundedCornerShape(4.dp))
									) {
										if (file.hasThumbnail && tempFiles[file.id]?.exists() == true) {
											AsyncImage(
												model = tempFiles[file.id]?.absolutePath,
												contentDescription = null,
												contentScale = ContentScale.Crop,
												modifier = Modifier.fillMaxSize(),
											)

											Box(
												modifier = Modifier
													.align(Alignment.BottomEnd)
													.padding(4.dp)
													.background(
														Color.Black.copy(alpha = 0.6f),
														RoundedCornerShape(4.dp)
													)
													.padding(4.dp)
											) {
												Icon(
													painter = painterResource(getFileIcon(file.name)),
													contentDescription = null,
													modifier = Modifier.size(20.dp),
													tint = Color.White
												)
											}
										} else {
											Box(
												Modifier
													.fillMaxSize()
													.background(
														brush = Brush.verticalGradient(
															colors = listOf(
																Color(0xFF1E3A8A).copy(0.25f),
																Color(0xFF3B82F6).copy(0.25f)
															)
														)
													),
												contentAlignment = Alignment.Center
											) {
												Icon(
													painter = painterResource(getFileIcon(file.name)),
													contentDescription = null,
													modifier = Modifier.size(48.dp),
													tint = Color.White.copy(alpha = 0.5f)
												)
											}
										}
									}
								}
								Row(
									modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
									verticalAlignment = Alignment.CenterVertically
								) {
									AsyncImage(
										model = "https://i.pravatar.cc/150?u=User$index",
										contentDescription = null,
										contentScale = ContentScale.Crop,
										modifier = Modifier.size(24.dp).clip(CircleShape)
									)
								}
							}
						}
					}
				}
			}
		}
	}
}

fun createTransferable(text: String): DragAndDropTransferable {
	// Desktop (Swing/AWT) cần Transferable interface
	val stringSelection = StringSelection(text)
	return DragAndDropTransferable(stringSelection)
}