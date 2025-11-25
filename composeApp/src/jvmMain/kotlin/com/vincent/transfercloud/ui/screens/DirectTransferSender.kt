package com.vincent.transfercloud.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.HeadLineLarge
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import com.vincent.transfercloud.ui.viewModel.DirectTransferSendVM
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
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
	val expanded by rememberSaveable { mutableStateOf(true) }
	var isDragging by remember { mutableStateOf(false) }
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes("files/empty_state.json").decodeToString()
		)
	}
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
					val pending = files.first() as File
					if (pending.length() > 10 * 1024 * 1024) {
						scope.launch {
							scaffoldState.snackbarHostState.showSnackbar(
								"File size exceeds 10MB limit.",
								actionLabel = "Hide",
								duration = SnackbarDuration.Short
							)
						}
						return false
					}
					viewModel.addTransferFiles(files.map { it as File })
				}
				return true
			}
		}
	}

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
					Card(
						modifier = Modifier.fillMaxWidth(),
						shape = RoundedCornerShape(12.dp),
						elevation = CardDefaults.cardElevation(
							defaultElevation = 4.dp,
							hoveredElevation = 2.dp
						),
					) {
						Column(
							Modifier.fillMaxWidth().padding(16.dp).height(128.dp),
							verticalArrangement = Arrangement.spacedBy(8.dp)
						) {
							if (uploadingFiles.isEmpty()) Text(
								"There is no uploading file.",
								color = MaterialTheme.colorScheme.onSurfaceVariant,
								style = TitleLineLarge.copy(fontWeight = FontWeight.W500)
							)
							else {
								Text(
									"Uploading Files:",
									color = MaterialTheme.colorScheme.onSurfaceVariant,
									style = TitleLineLarge.copy(fontWeight = FontWeight.W500, lineHeight = 18.sp)
								)
								LazyRow(
									horizontalArrangement = Arrangement.spacedBy(8.dp),
									modifier = Modifier.padding(top = 8.dp)
								) {
									items(uploadingFiles) { file ->
										Box(
											modifier = Modifier
												.height(100.dp)
												.clip(RoundedCornerShape(8.dp))
										) {
											if (file.isImage()) {
												AsyncImage(
													model = file.absolutePath,
													contentDescription = null,
													contentScale = ContentScale.Crop,
													modifier = Modifier.fillMaxSize()
												)
											} else {
												// Icon cho file không phải hình
												Box(
													Modifier
														.fillMaxSize()
														.background(Color.Gray.copy(alpha = 0.4f))
												) {
													Icon(
														imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
														contentDescription = null,
														modifier = Modifier
															.aspectRatio(1f)
															.align(Alignment.Center),
														tint = Color.White
													)
												}
											}

											IconButton(
												onClick = { viewModel.removeTransferFile(file) },
												modifier = Modifier
													.size(16.dp)
													.align(Alignment.TopEnd)
											) {
												Icon(
													imageVector = Icons.Default.Close,
													contentDescription = "Remove",
													modifier = Modifier.size(12.dp)
												)
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
							if (uploadingFiles.isEmpty()) scope.launch {
								scaffoldState.snackbarHostState.showSnackbar(
									"Please add files to send first.",
									actionLabel = "OK",
									duration = SnackbarDuration.Short
								)
							} else viewModel.transferTo(device)
						}
					) {
						Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
							AsyncImage(
								model = device.fromAvatar,
								contentDescription = "Device Avatar",
								contentScale = ContentScale.Crop,
								modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
								fallback = painterResource(Res.drawable.compose_multiplatform)
							)

							Column((Modifier.fillMaxHeight())) {
								Text(device.fromName, style = TitleLineBig.copy(fontSize = 17.sp))
								Text(device.tcpHost, style = TitleLineLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.W500))
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
					tooltip = { PlainTooltip { Text("Clear selected") } },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeadingContent(navigator: Navigator) {
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description") } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { if (navigator.canPop) navigator.pop() }) {
			Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Localized description")
		}
	}
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description") } },
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
		tooltip = { PlainTooltip { Text("Localized description") } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { /* doSomething() */ }) {
			Icon(Icons.Filled.Download, contentDescription = "Localized description")
		}
	}
	TooltipBox(
		positionProvider =
			TooltipDefaults.rememberTooltipPositionProvider(),
		tooltip = { PlainTooltip { Text("Localized description") } },
		state = rememberTooltipState(),
	) {
		IconButton(onClick = { /* doSomething() */ }) {
			Icon(Icons.Filled.Favorite, contentDescription = "Localized description")
		}
	}
}

fun File.isImage(): Boolean {
	val lower = name.lowercase()
	return lower.endsWith(".jpg") ||
			lower.endsWith(".jpeg") ||
			lower.endsWith(".png") ||
			lower.endsWith(".gif") ||
			lower.endsWith(".webp")
}
