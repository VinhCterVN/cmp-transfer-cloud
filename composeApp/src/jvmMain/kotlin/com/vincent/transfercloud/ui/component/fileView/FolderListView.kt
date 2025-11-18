package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.component.dialog.FileOptionMenu
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.utils.formatIsoToMonthDay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.empty_state_empty_folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.FolderListView(
	listState: LazyListState,
	viewModel: FolderViewModel = koinInject<FolderViewModel>()
) {
	val scope = rememberCoroutineScope()
	val navigator = LocalNavigator.currentOrThrow
	val folderData by viewModel.folderData.collectAsState()
	val bottomSheetState = LocalBottomSheetScaffoldState.current
	val foldersExpanded = remember { mutableStateOf(true) }
	val filesExpanded = remember { mutableStateOf(true) }
	var openMenuFolderId by remember { mutableStateOf<String?>(null) }
	var expanded by remember { mutableStateOf(false) }
	var menuPosition by remember { mutableStateOf(Offset.Zero) }
	val tableHeadStyle = TextStyle(
		fontWeight = FontWeight.SemiBold,
		fontSize = 14.sp,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
	val tableRowStyle = TextStyle(
		fontSize = 13.sp,
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
					itemsIndexed(folderData?.subfolders ?: emptyList()) { index, folder ->
						val isLast = index == (folderData?.subfolders?.lastIndex ?: -1)
						val color = MaterialTheme.colorScheme.outlineVariant
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clip(RoundedCornerShape(8.dp))
								.clickable {
									scope.launch { navigator.push(FolderDetailView(folder.id)) }
								}
								.background(
									if (index % 2 == 0) Color.Transparent
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
								.pointerInput(Unit) {
									awaitPointerEventScope {
										while (true) {
											val event = awaitPointerEvent()
											if (event.buttons.isSecondaryPressed) {
												val position = event.changes.first().position
												menuPosition = position
												expanded = true
											}
											if (event.buttons.isForwardPressed) {
												println("isForwardPressed")
											}
										}
									}
								}
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
									modifier = Modifier.size(20.dp)
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
									onMove = { openMenuFolderId = null }, onShare = { openMenuFolderId = null }, onDelete = {
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
						DropdownMenu(
							expanded = expanded,
							onDismissRequest = { expanded = false },
							offset = DpOffset(menuPosition.x.dp, menuPosition.y.dp)
						) {
							DropdownMenuItem(
								text = { Text("Rename") },
								onClick = { /* ... */ }
							)
							DropdownMenuItem(
								text = { Text("Delete") },
								onClick = { /* ... */ }
							)
						}
					}
				}
			}
			item {
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
				// Files table header (reuse same structure)
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
					// Files list items (demo with 20 items)
					itemsIndexed(folderData?.files ?: emptyList()) { index, file ->
						val isLast = index == (folderData?.subfolders?.lastIndex ?: -1)
						val color = MaterialTheme.colorScheme.outlineVariant

						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clip(RoundedCornerShape(8.dp))
								.clickable { /*TODO: HANDLE FILE ONCLICK*/ }
								.background(
									if (index % 2 == 0) Color.Transparent
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
								.padding(horizontal = 16.dp, vertical = 8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Row(
								modifier = Modifier.weight(1f),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.spacedBy(12.dp)
							) {
								Icon(
									Icons.AutoMirrored.Filled.InsertDriveFile,
									null,
									tint = MaterialTheme.colorScheme.onSurfaceVariant,
									modifier = Modifier.size(20.dp)
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
								"${file.fileSize / 1024} KB",
								style = tableRowStyle,
								modifier = Modifier.weight(0.25f)
							)

							Text(
								file.name,
								style = tableRowStyle,
								modifier = Modifier.weight(0.3f)
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
									onMove = { openMenuFolderId = null }, onShare = { openMenuFolderId = null }, onDelete = {
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
		}
	}
}

data class TableHead(
	val name: String,
	val weight: Float
)