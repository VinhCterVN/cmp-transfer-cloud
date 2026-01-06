package com.vincent.transfercloud.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vincent.transfercloud.ui.component.dialog.*
import com.vincent.transfercloud.ui.component.fileView.FileChainView
import com.vincent.transfercloud.ui.component.fileView.FolderGridView
import com.vincent.transfercloud.ui.component.fileView.FolderListView
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.FileViewIndex
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.state.UIState
import com.vincent.transfercloud.ui.theme.LabelLineMedium
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolderUI(
	id: String,
	appState: AppState = koinInject<AppState>(),
	viewModel: FolderViewModel = koinInject<FolderViewModel>()
) {
	val scope = rememberCoroutineScope()
	val scaffoldState = LocalBottomSheetScaffoldState.current
	val navigator = LocalNavigator.currentOrThrow
	val isCreatingFolder by appState.isCreatingFolder.collectAsState()
	val gridState = rememberLazyGridState()
	val listState = rememberLazyListState()
	var showTargetBorder by remember { mutableStateOf(false) }
	var uploadingFile by remember { mutableStateOf<File?>(null) }
	val draggedItem by viewModel.draggedItem.collectAsState()
	val dragAndDropTarget = remember {
		object : DragAndDropTarget {
			override fun onStarted(event: DragAndDropEvent) {
				showTargetBorder = true
				val transferable = event.awtTransferable
				if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					val text = transferable.getTransferData(DataFlavor.stringFlavor) as String
					println("Dragging text: $text")
				}
				if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
					println("Dragging files: $files")
				}
			}

			override fun onEnded(event: DragAndDropEvent) {
				showTargetBorder = false
			}

			override fun onDrop(event: DragAndDropEvent): Boolean {
				println("Action at the target: ${event.action}")
				val transferable = event.awtTransferable
				if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
					val pending = files.first() as File
					val size = if (pending.isDirectory) getFolderSize(pending) else pending.length()
					if (size > 50 * 1024 * 1024) {
						scope.launch {
							scaffoldState.snackbarHostState.showSnackbar(
								"File size exceeds 50MB limit.",
								actionLabel = "Hide",
								duration = SnackbarDuration.Short
							)
						}
						return false
					}
					uploadingFile = pending
				}
				return true
			}
		}
	}
	var expanded by remember { mutableStateOf(false) }
	val focusRequester = FocusRequester()
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes("files/uploading.json").decodeToString()
		)
	}
	val fileOpen = rememberFilePickerLauncher { platformFile ->
		val file = platformFile?.file
		file?.let {
			scope.launch {
				if (it.length() > 50 * 1024 * 1024) {
					scaffoldState.snackbarHostState.showSnackbar(
						"File size exceeds 10MB limit.",
						actionLabel = "Hide",
						duration = SnackbarDuration.Short
					)
					return@launch
				}
				scaffoldState.snackbarHostState.showSnackbar(
					"Uploading file: ${it.name}",
					actionLabel = "Hide",
					duration = SnackbarDuration.Short
				)
				viewModel.uploadFile(it).join()
				scaffoldState.snackbarHostState.showSnackbar(
					"File ${it.name} uploaded successfully.",
					actionLabel = "Hide",
					duration = SnackbarDuration.Short
				)
			}
		}
	}
	val items = listOf(
		FloatingMenuItem(Icons.Filled.CreateNewFolder, "Create Folder") { appState.isCreatingFolder.value = true },
		FloatingMenuItem(Icons.Default.UploadFile, "New File") {
			fileOpen.launch()
		},
		FloatingMenuItem(Icons.Filled.SelectAll, "Select All") { viewModel.selectAll() },
		FloatingMenuItem(Icons.AutoMirrored.Filled.Label, "Label") {},
	)
	val viewIndex by viewModel.currentViewIndex.collectAsState()
	val uiState by viewModel.uiState.collectAsState()
	val blurRadius by animateDpAsState(
		targetValue = if (showTargetBorder) 4.dp else 0.dp,
		animationSpec = tween(300)
	)
	LaunchedEffect(Unit) {
		appState.currentFolder.emit(id)
		viewModel.setSelectedIds(emptySet())
	}

	Box(
		Modifier.fillMaxSize()
			.pointerInput(Unit) {
				awaitPointerEventScope {
					var lastItem: Screen? = null
					while (true) {
						val event = awaitPointerEvent()
						if (event.buttons.isBackPressed && navigator.canPop) {
							lastItem = navigator.lastItemOrNull
							navigator.pop()
						} else if (event.buttons.isForwardPressed) {
							if (lastItem != null) navigator.push(lastItem)
						}
					}
				}
			}
	) {
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
							IconButton(onClick = navigator::pop) {
								Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
							}
						},
						title = {}
					)
					Text("Error loading folder data.", style = TitleLineLarge)
					Spacer(Modifier.height(15.dp))
					Text((uiState as UIState.Error).message, style = LabelLineMedium)
					Spacer(Modifier.height(15.dp))
					ElevatedButton({
						scope.launch {
							viewModel.getFolderData()
						}
					}) {
						Text("Retry", style = TitleLineLarge)
					}
				}
			}

			else -> @Composable {
				FolderNameDialog(isCreatingFolder, onDismiss = {
					appState.isCreatingFolder.value = false;
					appState.renamingFolder.value = "" to ""
				})
				FileUploadDialog(
					uploadFile = uploadingFile,
					onCancel = { uploadingFile = null },
					action = { uploadingFile = null }
				)
				ShareFileDialog()
				Column(
					Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp),
				) {
					FileChainView()

					Box(Modifier.clip(RoundedCornerShape(12.dp)).blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)) {
						Column(Modifier.fillMaxSize()) {
							when (viewIndex) {
								FileViewIndex.LIST -> FolderListView(listState)
								FileViewIndex.GRID -> FolderGridView(gridState)
							}
						}
						Box(
							Modifier.fillMaxSize().zIndex(100f)
								.dragAndDropTarget(
									shouldStartDragAndDrop = { event ->
										draggedItem == null
									},
									target = dragAndDropTarget
								)
								.then(
									if (showTargetBorder) Modifier.border(
										2.dp,
										Color.Blue.copy(0.75f),
										RoundedCornerShape(12.dp)
									).background(
										MaterialTheme.colorScheme.primaryContainer.copy(0.75f),
										RoundedCornerShape(12.dp)
									) else Modifier
								)
						)
					}
				}

				VerticalScrollbar(
					modifier = Modifier.align(Alignment.CenterEnd)
						.fillMaxHeight()
						.width(6.dp),
					adapter = when (viewIndex) {
						FileViewIndex.LIST -> rememberScrollbarAdapter(listState)
						FileViewIndex.GRID -> rememberScrollbarAdapter(gridState)
					}
				)

				AnimatedVisibility(
					visible = showTargetBorder,
					enter = slideInVertically(
						initialOffsetY = { it },
						animationSpec = tween(300)
					) + fadeIn(
						animationSpec = tween(300),
						initialAlpha = 0f
					) + scaleIn(
						animationSpec = tween(300),
						initialScale = 0.8f
					),
					exit = slideOutVertically(
						targetOffsetY = { it },
						animationSpec = tween(300)
					) + fadeOut(
						animationSpec = tween(300),
						targetAlpha = 0f
					) + scaleOut(
						animationSpec = tween(300),
						targetScale = 0.8f
					),
					modifier = Modifier.align(Alignment.BottomCenter).zIndex(100f)
				) {
					Column(
						Modifier
							.width(300.dp)
							.aspectRatio(1f),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Image(
							painter = rememberLottiePainter(
								composition = composition,
								iterations = Compottie.IterateForever
							),
							contentDescription = null,
						)

						Box(
							Modifier
								.background(MaterialTheme.colorScheme.onPrimaryFixedVariant, RoundedCornerShape(12.dp))
								.padding(16.dp),
							contentAlignment = Alignment.Center
						) {
							Text(
								"Drop Files Here to Upload",
								style = TitleLineLarge.copy(
									color = Color.White
								)
							)
						}
					}
				}
				FloatingActionButtonMenu(
					expanded = expanded,
					modifier = Modifier.align(Alignment.BottomEnd),
					button = {
						ToggleFloatingActionButton(
							modifier = Modifier.semantics {
								traversalIndex = -1f
								stateDescription = if (expanded) "Expanded" else "Collapsed"
								contentDescription = "Toggle Menu"
							}.animateFloatingActionButton(
								visible = true,
								alignment = Alignment.BottomEnd
							).focusRequester(focusRequester = focusRequester),
							checked = expanded,
							onCheckedChange = { expanded = !expanded },
						) {
							val imageVector by remember {
								derivedStateOf { if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add }
							}

							Icon(
								painter = rememberVectorPainter(imageVector),
								contentDescription = null,
								modifier = Modifier.animateIcon({ checkedProgress })
							)
						}
					}
				) {
					items.forEachIndexed { i, item ->
						FloatingActionButtonMenuItem(
							modifier = Modifier.semantics {
								isTraversalGroup = true
								if (i == items.size - 1) {
									customActions = listOf(
										CustomAccessibilityAction(
											label = "Close Menu",
											action = {
												expanded = false
												true
											}
										))
								}
							}.then(
								if (i == 0) {
									Modifier.onKeyEvent {
										if (
											it.type == KeyEventType.KeyDown &&
											(it.key == Key.DirectionUp ||
													(it.isShiftPressed && it.key == Key.Tab))
										) {
											focusRequester.requestFocus()
											return@onKeyEvent true
										}
										return@onKeyEvent false
									}
								} else {
									Modifier
								}
							),
							onClick = item.onClick,
							icon = { Icon(item.icon, contentDescription = null) },
							text = { Text(text = item.text) }
						)
					}
				}

			}
		}
	}
}

data class FloatingMenuItem(
	val icon: ImageVector,
	val text: String,
	val onClick: () -> Unit
)