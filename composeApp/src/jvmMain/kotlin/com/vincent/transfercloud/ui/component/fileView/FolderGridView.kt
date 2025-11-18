package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vincent.transfercloud.ui.component.button.ExpandButton
import com.vincent.transfercloud.ui.component.dialog.FileOptionMenu
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.LabelLineSmall
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.empty_state_empty_folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.FolderGridView(
	listState: LazyGridState,
	viewModel: FolderViewModel = koinInject<FolderViewModel>()
) {
	val scope = rememberCoroutineScope()
	val navigator = LocalNavigator.currentOrThrow
	val bottomSheetState = LocalBottomSheetScaffoldState.current
	val folderData by viewModel.folderData.collectAsState()
	val foldersExpanded = remember { mutableStateOf(true) }
	val filesExpanded = remember { mutableStateOf(true) }
	var openMenuFolderId by remember { mutableStateOf<String?>(null) }

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
			if (!folderData?.subfolders.isNullOrEmpty()) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ExpandButton(
						foldersExpanded,
						"Folders (${folderData?.subfolders?.size ?: 0})"
					)
				}
				if (foldersExpanded.value) {
					itemsIndexed(folderData?.subfolders ?: emptyList()) { index, folder ->
						Card(
							onClick = {
								scope.launch { navigator.push(FolderDetailView(folder.id)) }
							},
							colors = CardDefaults.cardColors(
								containerColor = MaterialTheme.colorScheme.surfaceVariant
							),
							elevation = CardDefaults.cardElevation(2.dp),
							shape = RoundedCornerShape(12.dp),
							modifier = Modifier.padding(8.dp).height(55.dp)
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
										modifier = Modifier
											.clip(CircleShape)
											.pointerHoverIcon(PointerIcon.Hand)
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
						}
					}
				}
			}
			if (!folderData?.files.isNullOrEmpty()) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ExpandButton(filesExpanded, "Files (${folderData?.files?.size ?: 0})")
				}
				if (filesExpanded.value) {
					itemsIndexed(folderData?.files ?: emptyList()) { index, file ->
						Card(
							onClick = {},
							colors = CardDefaults.cardColors(
								containerColor = MaterialTheme.colorScheme.surfaceVariant
							),
							elevation = CardDefaults.cardElevation(2.dp),
							shape = RoundedCornerShape(12.dp),
							modifier = Modifier.padding(8.dp).aspectRatio(1f)
						) {
							// File content here
							Text(file.name)
						}
					}
				}
			}
		}
	}

}