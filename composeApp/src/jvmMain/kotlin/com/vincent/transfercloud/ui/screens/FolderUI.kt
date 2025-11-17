package com.vincent.transfercloud.ui.screens

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vincent.transfercloud.ui.component.dialog.CreateFolderDialog
import com.vincent.transfercloud.ui.component.fileView.FileChainView
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import com.vincent.transfercloud.ui.theme.LabelLineSmall
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import java.awt.datatransfer.DataFlavor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolderView(
	id: String,
	appState: AppState = koinInject<AppState>(),
	viewModel: FolderViewModel = koinInject<FolderViewModel>()
) {
	val navigator = LocalNavigator.currentOrThrow
	val listState = rememberLazyGridState()
	val scope = rememberCoroutineScope()
	val coroutineScope = rememberCoroutineScope()
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes("files/empty.json").decodeToString()
		)
	}
	var showTargetBorder by remember { mutableStateOf(false) }
	var targetText by remember { mutableStateOf("Drop Here") }
	val dragAndDropTarget = remember {
		object : DragAndDropTarget {
			override fun onStarted(event: DragAndDropEvent) {
				showTargetBorder = true
			}

			override fun onEnded(event: DragAndDropEvent) {
				showTargetBorder = false
			}

			override fun onDrop(event: DragAndDropEvent): Boolean {
				println("Action at the target: ${event.action}")
				val result = (targetText == "Drop Here")
				targetText = event.awtTransferable.let {
					if (it.isDataFlavorSupported(DataFlavor.stringFlavor)) {
						it.getTransferData(DataFlavor.stringFlavor) as String
					} else {
						it.transferDataFlavors.first().humanPresentableName
					}
				}
				coroutineScope.launch {
					delay(2000)
					targetText = "Drop Here"
				}
				return result
			}
		}
	}
	val uiState by viewModel.uiState.collectAsState()
	val folderData by viewModel.folderData.collectAsState()

	LaunchedEffect(Unit) {
		appState.currentFolder.emit(id)
		viewModel.getFolderData(id)
	}


	Box(Modifier.fillMaxSize()) {
		when (uiState) {
			is UIState.Loading -> @Composable {
				Box(
					Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					ContainedLoadingIndicator(modifier = Modifier.size(150.dp))
				}
			}

			is UIState.Error -> @Composable {
				Column(
					Modifier.fillMaxSize(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					TopAppBar(
						navigationIcon = {
							IconButton(
								onClick = {
									navigator.pop()
								}
							) {
								Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
							}
						},
						title = {
						}
					)
					Text("Error loading folder data.")
				}
			}

			else -> @Composable {
				Column(
					Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp),
				) {
					FileChainView()
					LazyVerticalGrid(
						state = listState,
						columns = GridCells.Adaptive(minSize = 250.dp),
						contentPadding = PaddingValues(8.dp),
						modifier = Modifier.weight(1f).dragAndDropTarget(
							shouldStartDragAndDrop = { event ->
								println(event.action)
								true
							},
							target = dragAndDropTarget
						).then(
							if (showTargetBorder) Modifier.border(
								2.dp,
								MaterialTheme.colorScheme.primaryContainer,
								RoundedCornerShape(12.dp)
							) else Modifier
						)
					) {
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
											folder.name, style = TextStyle(
												fontWeight = FontWeight.SemiBold,
												fontSize = 16.sp
											)
										)

										Text(
											folder.createdAt,
											style = LabelLineSmall.copy(fontWeight = FontWeight.Normal, fontSize = 12.sp)
										)
									}
									Spacer(Modifier.weight(1f))

									Box(
										modifier = Modifier
											.clip(CircleShape)
											.pointerHoverIcon(PointerIcon.Hand)
											.clickable {}
											.padding(4.dp)
									) {
										Icon(
											Icons.Default.MoreVert,
											null,
											tint = MaterialTheme.colorScheme.onSurfaceVariant,
											modifier = Modifier.size(18.dp)
										)
									}
								}
							}
						}
					}
				}

				VerticalScrollbar(
					modifier = Modifier.align(Alignment.CenterEnd)
						.fillMaxHeight()
						.width(6.dp),
					adapter = rememberScrollbarAdapter(listState),
				)
			}
		}
	}

	CreateFolderDialog()
}
