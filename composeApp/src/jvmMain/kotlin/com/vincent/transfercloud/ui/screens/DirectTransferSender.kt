package com.vincent.transfercloud.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.component.FilePreviewCard
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.HeadLineLarge
import com.vincent.transfercloud.ui.theme.LabelLineMedium
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import com.vincent.transfercloud.ui.viewModel.DirectTransferSendVM
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.compose_multiplatform
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun DirectTransferSendUI(
	viewModel: DirectTransferSendVM = koinViewModel()
) {
	val scope = rememberCoroutineScope()
	val infiniteTransition = rememberInfiniteTransition()
	val navigator = LocalNavigator.currentOrThrow
	val scaffoldState = LocalBottomSheetScaffoldState.current
	val devices by viewModel.availableReceivers.collectAsState()
	val uploadingFiles by viewModel.uploadingFiles.collectAsState()
	val uploadingToId by viewModel.uploadingToId.collectAsState()
	val isUploading by viewModel.isUploading.collectAsState()
	val sendingProgress by viewModel.sendingProgress.collectAsState()
	val bytesProgress by viewModel.bytesProgress.collectAsState()
	val expanded by rememberSaveable { mutableStateOf(true) }
	var isDragging by remember { mutableStateOf(false) }
	val progressPercentage = remember(bytesProgress) {
		val (sent, total) = bytesProgress
		if (total > 0L) sent.toFloat() / total.toFloat() else 0f
	}
	val animatedProgress by animateFloatAsState(
		targetValue = progressPercentage,
		animationSpec = tween(
			durationMillis = 300,
			easing = FastOutSlowInEasing
		)
	)
	val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
	val emptyIconColor by infiniteTransition.animateColor(
		initialValue = surfaceVariant,
		targetValue = surfaceVariant,
		animationSpec = infiniteRepeatable(
			animation = keyframes {
				durationMillis = 3000
				surfaceVariant at 0
				Color(0xFF7A7979) at 1500
				Color(0xFF8C8C8C) at 3000
			},
			repeatMode = RepeatMode.Restart
		),
		label = "emptyIconColor"
	)
	val dragAndDropTarget = remember {
		object : DragAndDropTarget {
			override fun onStarted(event: DragAndDropEvent) {
				isDragging = true
			}

			override fun onEnded(event: DragAndDropEvent) {
				isDragging = false
			}

			override fun onDrop(event: DragAndDropEvent): Boolean {
				val transferable = event.awtTransferable
				if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
					val oversizedFiles = files.filter {
						(it as File).length() > 1L * 1024L * 1024L * 1024L
					}
					if (oversizedFiles.isNotEmpty()) {
						scope.launch {
							scaffoldState.snackbarHostState.showSnackbar(
								"File size exceeds 1GB limit.",
								actionLabel = "Hide",
								duration = SnackbarDuration.Short
							)
						}
					}
					viewModel.addTransferFiles(files.map { it as File })
				}
				return true
			}
		}
	}
	val fileOpen = rememberFilePickerLauncher(mode = FileKitMode.Multiple()) { platformFiles ->
		val files = platformFiles?.map { it.file }
		files?.let { viewModel.addTransferFiles(it) }
	}
	val chooseIcons = listOf(
		FileSelectIcon(
			icon = Icons.Filled.FilePresent,
			text = "File",
			onClick = { fileOpen.launch() }
		),
	)

	Box(
		Modifier.fillMaxSize()
			.dragAndDropTarget(
				shouldStartDragAndDrop = { true },
				target = dragAndDropTarget
			),
		contentAlignment = Alignment.Center
	) {
		if (isDragging) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Column(
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				AnimatedVisibility(
					visible = isDragging,
					enter = slideInVertically(
						animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
					) + fadeIn(
						animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()
					)
				) {
					Icon(Icons.Default.Download, null, Modifier.size(50.dp))
				}
				Text("Drop files to send", style = HeadLineLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
			}
		}
		else
			LazyColumn(
				Modifier.fillMaxHeight().fillMaxWidth(0.75f).padding(vertical = 24.dp, horizontal = 12.dp),
//				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				item {
					Text(
						"Direct Sending Data...",
						style = HeadLineLarge,
						textAlign = TextAlign.Center,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.fillMaxWidth()
					)
				}

				item {
					Spacer(Modifier.height(25.dp))
				}

				item {
					Card(
						modifier = Modifier.fillMaxWidth(),
						shape = RoundedCornerShape(12.dp),
						elevation = CardDefaults.cardElevation(
							defaultElevation = 4.dp,
							hoveredElevation = 2.dp
						),
						colors = CardDefaults.cardColors()
					) {
						Column(
							Modifier.fillMaxWidth()
								.background(MaterialTheme.colorScheme.surfaceContainerLow.copy(0.4f))
								.padding(16.dp).wrapContentHeight(),
							verticalArrangement = Arrangement.SpaceBetween
						) {
							if (uploadingFiles.isEmpty()) {
								Text(
									"There is no uploading file.",
									color = MaterialTheme.colorScheme.onSurfaceVariant,
									style = TitleLineLarge.copy(fontWeight = FontWeight.W500)
								)
								Spacer(Modifier.height(10.dp))
								LazyRow(
									horizontalArrangement = Arrangement.spacedBy(8.dp)
								) {
									items(chooseIcons) { item ->
										Card(
											modifier = Modifier.padding(4.dp),
											shape = RoundedCornerShape(8.dp),
											elevation = CardDefaults.cardElevation(4.dp),
											onClick = item.onClick
										) {
											Column(
												Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(0.85f))
													.padding(horizontal = 24.dp, vertical = 12.dp),
												horizontalAlignment = Alignment.CenterHorizontally,
												verticalArrangement = Arrangement.spacedBy(8.dp)
											) {
												Icon(item.icon, null, Modifier.size(36.dp))
												Text(item.text, style = LabelLineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
											}
										}
									}
								}

							} else {
								Row(
									Modifier.fillMaxWidth(),
									horizontalArrangement = Arrangement.SpaceBetween,
									verticalAlignment = Alignment.CenterVertically
								) {
									Text(
										"Uploading Files:",
										color = MaterialTheme.colorScheme.onSurfaceVariant,
										style = MaterialTheme.typography.titleMedium
									)

									Text(
										"Selected ${uploadingFiles.size} files",
										color = MaterialTheme.colorScheme.primary,
										style = MaterialTheme.typography.titleMedium
									)
									val totalSize = uploadingFiles.sumOf { it.length() }
									Text(
										"Total ${
											if (totalSize < 1024L) "$totalSize B"
											else if (totalSize < 1024L * 1024L) "${totalSize / 1024L} KB"
											else if (totalSize < 1024L * 1024L * 1024L) "${totalSize / (1024L * 1024L)} MB"
											else "${totalSize / (1024L * 1024L * 1024L)} GB"
										}",
										color = MaterialTheme.colorScheme.primary,
										style = MaterialTheme.typography.titleMedium
									)
								}
								LazyRow(
									horizontalArrangement = Arrangement.spacedBy(8.dp),
									modifier = Modifier.padding(top = 8.dp)
								) {
									items(uploadingFiles) { file ->
										FilePreviewCard(
											file = file,
											onRemove = { viewModel.removeTransferFile(file) }
										)
									}

									item {
										Box(
											modifier = Modifier
												.height(100.dp)
												.clip(RoundedCornerShape(8.dp)),
											contentAlignment = Alignment.Center
										) {
											IconButton(onClick = { fileOpen.launch() }) {
												Icon(Icons.Default.Add, null)
											}
										}
									}
								}
							}
						}
					}
				}

				item {
					Spacer(Modifier.height(8.dp))
					Text(
						"Available devices", style = TitleLineLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.padding(start = 24.dp)
					)
				}

				if (devices.isEmpty())
					item {
						Card(
							modifier = Modifier.fillMaxWidth(),
							shape = RoundedCornerShape(12.dp),
							elevation = CardDefaults.cardElevation(
								defaultElevation = 4.dp,
								hoveredElevation = 2.dp
							),
							onClick = {}
						) {
							Row(Modifier.fillMaxSize().padding(24.dp)) {
								Icon(Icons.Default.Laptop, null, Modifier.size(64.dp), tint = emptyIconColor)
							}
						}
					}
				else itemsIndexed(devices) { index, device ->
					Card(
						modifier = Modifier.fillMaxWidth(),
						shape = RoundedCornerShape(12.dp),
						elevation = CardDefaults.cardElevation(
							defaultElevation = 4.dp,
							hoveredElevation = 2.dp
						),
						onClick = {
							scope.launch {
								if (isUploading) {
									scaffoldState.snackbarHostState.showSnackbar(
										"Currently sending files. Please wait until the current transfer is complete.",
										actionLabel = "OK",
										duration = SnackbarDuration.Short
									)
									return@launch
								}
								if (uploadingFiles.isEmpty())
									scaffoldState.snackbarHostState.showSnackbar(
										"Please add files to send first.",
										actionLabel = "OK",
										duration = SnackbarDuration.Short
									)
								else {
									viewModel.transferTo(device).join()
									scaffoldState.snackbarHostState.showSnackbar(
										"Sent to ${device.fromName}",
										actionLabel = "OK",
										duration = SnackbarDuration.Short
									)

								}
							}
						}
					) {
						Column {
							Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
								AsyncImage(
									model = device.fromAvatar,
									contentDescription = "Device Avatar",
									contentScale = ContentScale.Crop,
									modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
									fallback = painterResource(Res.drawable.compose_multiplatform)
								)
								Column((Modifier.fillMaxHeight())) {
									Text(
										device.fromName,
										style = TitleLineBig.copy(fontSize = 17.sp),
										color = MaterialTheme.colorScheme.onSurface
									)
									Text(
										device.tcpHost,
										style = TitleLineLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.W400),
										color = MaterialTheme.colorScheme.onSurface
									)
									Text(device.fromDeviceName, color = MaterialTheme.colorScheme.onSurface)
								}
								Spacer(Modifier.weight(1f))

								Column(Modifier.align(Alignment.CenterVertically), verticalArrangement = Arrangement.spacedBy(6.dp)) {
									if (!isUploading) {
										Text("Tap to send", style = TitleLineBig, color = MaterialTheme.colorScheme.primary)
									} else {
										// 2 Texts for files and bytes
										Text(
											"Sending ${sendingProgress.first + 1} / ${sendingProgress.second} files",
											style = TitleLineLarge.copy(fontSize = 14.sp),
											color = MaterialTheme.colorScheme.onSurfaceVariant
										)
										Text(
											"${(progressPercentage * 100).toInt()}% (${bytesProgress.first / (1024 * 1024)} / ${bytesProgress.second / (1024 * 1024)} MB)",
											style = TitleLineLarge.copy(fontSize = 14.sp),
											color = MaterialTheme.colorScheme.onSurfaceVariant
										)
									}
								}
							}
							if (isUploading && device.fromId == uploadingToId) {
								LinearProgressIndicator(
									progress = { animatedProgress },
									modifier = Modifier
										.fillMaxWidth()
										.height(4.dp)
										.clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
								)
							}
						}
					}
				}
			}

		HorizontalFloatingToolbar(
			modifier = Modifier.align(Alignment.BottomCenter),
			expanded = expanded,
			leadingContent = { LeadingContent(navigator) },
			trailingContent = { TrailingContent() },
			content = {
				TooltipBox(
					state = rememberTooltipState(),
					positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
					tooltip = { PlainTooltip { Text("Clear selected", color = MaterialTheme.colorScheme.surface) } },
				) {
					FilledIconButton(
						modifier = Modifier.width(64.dp),
						onClick = { }
					) {
						Icon(Icons.Filled.Add, null)
					}
				}
			},
		)
	}
}

data class FileSelectIcon(
	val icon: ImageVector,
	val text: String,
	val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeadingContent(navigator: Navigator) {
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description", color = MaterialTheme.colorScheme.surface) } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { if (navigator.canPop) navigator.pop() }) {
			Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Localized description")
		}
	}
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description", color = MaterialTheme.colorScheme.surface) } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { /* doSomething() */ }) {
			Icon(Icons.Filled.Edit, contentDescription = "Localized description")
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrailingContent() {
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description", color = MaterialTheme.colorScheme.surface) } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { /* doSomething() */ }) {
			Icon(Icons.Filled.Download, contentDescription = "Localized description")
		}
	}
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description", color = MaterialTheme.colorScheme.surface) } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { /* doSomething() */ }) {
			Icon(Icons.Filled.Favorite, contentDescription = "Localized description")
		}
	}
}
