package com.vincent.transfercloud.ui.component.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.vincent.transfercloud.core.server.DirectTransferSend
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.HeadLineMedium
import com.vincent.transfercloud.ui.viewModel.DirectTransferReceiveVM
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import transfercloud.composeapp.generated.resources.Res
import java.awt.Desktop
import java.io.File
import java.time.Instant

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReceiveDialog(
	visible: Boolean,
	onDismissRequest: () -> Unit,
	viewModel: DirectTransferReceiveVM = koinViewModel()
) {
	if (!visible) return
	val receivedFiles by viewModel.receivedData.collectAsState()
	var expandedItemId by remember { mutableStateOf<String?>(null) }
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes("files/dual-transfer.json").decodeToString()
		)
	}

	Dialog(onDismissRequest = onDismissRequest) {
		Card(
			shape = RoundedCornerShape(8.dp),
			elevation = CardDefaults.cardElevation(4.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
		) {
			Column(
				Modifier.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				Text("Transfer Received Files", style = HeadLineMedium, color = MaterialTheme.colorScheme.onSurface)

				LazyColumn(
					Modifier.heightIn(max = 400.dp).fillMaxWidth().padding(vertical = 8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					items(
						items = receivedFiles.values.toList(),
						key = { it.id }
					) { transfer ->
						TransferItem(
							transfer = transfer,
							isExpanded = expandedItemId == transfer.id,
							onExpandChange = { expanded ->
								expandedItemId = if (expanded) transfer.id else null
							},
							onDelete = { viewModel.deleteReceiveData(transfer.id) }
						)
					}

					if (receivedFiles.values.isEmpty())
						item {
							Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
								Image(
									painter = rememberLottiePainter(
										composition = composition,
										iterations = Compottie.IterateForever
									),
									contentDescription = null,
									modifier = Modifier.widthIn(max = 400.dp, min = 100.dp)
								)
							}
						}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferItem(
	transfer: DirectTransferSend,
	isExpanded: Boolean,
	onExpandChange: (Boolean) -> Unit,
	onDelete: () -> Unit
) {
	var showMenu by remember { mutableStateOf(false) }
	var showSaveDialog by remember { mutableStateOf(false) }

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.animateContentSize(
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioMediumBouncy,
					stiffness = Spring.StiffnessLow
				)
			),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
	) {
		Column {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				// Avatar
				Box(
					modifier = Modifier
						.size(48.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.primaryContainer),
					contentAlignment = Alignment.Center
				) {
					AsyncImage(
						model = transfer.fromAvatar,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxSize()
					)
				}
				// Info
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					Text(
						text = transfer.fromName,
						style = MaterialTheme.typography.titleMedium
					)

					Row {
						val formattedTime = Instant
							.ofEpochSecond(transfer.transferTime)
							.atZone(java.time.ZoneId.systemDefault())
							.toLocalDateTime()
							.toString()
						TooltipBox(
							positionProvider =
								TooltipDefaults.rememberTooltipPositionProvider(),
							tooltip = { PlainTooltip { Text(formattedTime, color = MaterialTheme.colorScheme.surface) } },
							state = rememberTooltipState(),
						) {
							Text(
								text = "${timeAgo(transfer.transferTime)} | ",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}

						Text(
							"${transfer.files.size} file${if (transfer.files.size > 1) "s" else ""}",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							modifier = Modifier.combinedClickable(
								enabled = true,
								indication = null,
								interactionSource = null,
								onClick = {
									onExpandChange(!isExpanded)
									showMenu = false
								}
							)
						)
					}
				}
				// Action button with dropdown
				Box {
					IconButton(onClick = { showMenu = true }) {
						Icon(
							imageVector = Icons.Default.MoreVert,
							contentDescription = "More options"
						)
					}

					DropdownMenu(
						expanded = showMenu,
						onDismissRequest = { showMenu = false }
					) {
						DropdownMenuItem(
							text = { Text("View Files") },
							onClick = {
								onExpandChange(!isExpanded)
								showMenu = false
							},
							leadingIcon = {
								Icon(
									imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
									contentDescription = null
								)
							}
						)

						DropdownMenuItem(
							text = { Text("Open Location") },
							onClick = {
								showMenu = false
								try {
									Desktop.getDesktop().open(File("C:\\TransferCloud\\DirectTransfer\\Received\\${transfer.id}"))
								} catch (e: Exception) {
									e.printStackTrace()
								}
							},
							leadingIcon = {
								Icon(
									imageVector = Icons.Default.FolderOpen,
									contentDescription = null
								)
							}
						)

						DropdownMenuItem(
							text = { Text("Save to...") },
							onClick = {
								showMenu = false
								showSaveDialog = true
							},
							leadingIcon = {
								Icon(
									imageVector = Icons.Default.SaveAs,
									contentDescription = null
								)
							}
						)

						HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

						DropdownMenuItem(
							text = { Text("Delete") },
							onClick = {
								showMenu = false
								onDelete()
							},
							leadingIcon = {
								Icon(
									imageVector = Icons.Default.Delete,
									contentDescription = null,
									tint = MaterialTheme.colorScheme.error
								)
							},
							colors = MenuDefaults.itemColors(
								textColor = MaterialTheme.colorScheme.error
							)
						)
					}
				}
			}
			// Expandable files section
			AnimatedVisibility(
				visible = isExpanded,
				enter = expandVertically(
					animationSpec = spring(
						dampingRatio = Spring.DampingRatioMediumBouncy,
						stiffness = Spring.StiffnessLow
					)
				),
				exit = shrinkVertically(
					animationSpec = spring(
						dampingRatio = Spring.DampingRatioMediumBouncy,
						stiffness = Spring.StiffnessLow
					)
				)
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 12.dp)
						.padding(bottom = 12.dp)
				) {
					Divider(modifier = Modifier.padding(vertical = 8.dp))

					Text(
						text = "Files",
						style = MaterialTheme.typography.labelMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.padding(bottom = 8.dp)
					)

					LazyRow(
						modifier = Modifier
							.fillMaxWidth()
							.height(100.dp),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						items(transfer.files) { filePath ->
							val fileName = File(filePath).name
							FileIconItem(fileName = fileName, filePath = filePath)
						}
					}
				}
			}
		}
	}
	// Save to dialog
	if (showSaveDialog) {
		SaveToDialog(
			transfer = transfer,
			onDismiss = { showSaveDialog = false }
		)
	}
}

@Composable
fun SaveToDialog(
	transfer: DirectTransferSend,
	onDismiss: () -> Unit
) {
	val scope = rememberCoroutineScope()
	var selectedPath by remember { mutableStateOf<String?>(null) }
	var isSaving by remember { mutableStateOf(false) }
	var saveResult by remember { mutableStateOf<SaveResult?>(null) }

	AlertDialog(
		onDismissRequest = { if (!isSaving) onDismiss() },
		icon = {
			Icon(
				imageVector = Icons.Default.SaveAs,
				contentDescription = null
			)
		},
		title = { Text("Save Files To") },
		text = {
			Column(
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				Text("Select a destination folder to save all files")
				if (selectedPath != null) {
					Card(
						colors = CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.surfaceVariant
						)
					) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(12.dp),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(8.dp)
						) {
							Icon(
								imageVector = Icons.Default.Folder,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier.size(24.dp)
							)
							Text(
								text = selectedPath ?: "",
								style = MaterialTheme.typography.bodySmall,
								modifier = Modifier.weight(1f)
							)
						}
					}
				}
				// Save result
				if (saveResult != null) {
					Card(
						colors = CardDefaults.cardColors(
							containerColor = if (saveResult!!.success) {
								MaterialTheme.colorScheme.primaryContainer
							} else {
								MaterialTheme.colorScheme.errorContainer
							}
						)
					) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(12.dp),
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Icon(
								imageVector = if (saveResult!!.success) Icons.Default.CheckCircle else Icons.Default.Error,
								contentDescription = null,
								tint = if (saveResult!!.success) {
									MaterialTheme.colorScheme.onPrimaryContainer
								} else {
									MaterialTheme.colorScheme.onErrorContainer
								}
							)
							Text(
								text = saveResult!!.message,
								style = MaterialTheme.typography.bodySmall,
								color = if (saveResult!!.success) {
									MaterialTheme.colorScheme.onPrimaryContainer
								} else {
									MaterialTheme.colorScheme.onErrorContainer
								}
							)
						}
					}
				}
				// Loading indicator
				if (isSaving) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
						verticalAlignment = Alignment.CenterVertically
					) {
						CircularProgressIndicator(modifier = Modifier.size(24.dp))
						Spacer(modifier = Modifier.width(12.dp))
						Text("Saving files...", style = MaterialTheme.typography.bodySmall)
					}
				}
			}
		},
		confirmButton = {
			if (saveResult?.success == true) {
				TextButton(onClick = onDismiss) {
					Text("Done")
				}
			} else {
				TextButton(
					onClick = {
						if (selectedPath != null && !isSaving) {
							isSaving = true
							saveResult = null
							val result = saveFilesToDirectory(
								sourceDir = "C:\\TransferCloud\\DirectTransfer\\Received\\${transfer.id}",
								destinationDir = selectedPath!!
							)

							saveResult = result
							isSaving = false
						} else {
							scope.launch {
								val chooser = FileKit.openDirectoryPicker("Select Destination Folder")
								selectedPath = chooser?.absolutePath()
							}
						}
					},
					enabled = !isSaving
				) {
					Text(if (selectedPath == null) "Choose Folder" else "Save")
				}
			}
		},
		dismissButton = {
			if (saveResult?.success != true) {
				TextButton(
					onClick = onDismiss,
					enabled = !isSaving
				) {
					Text("Cancel")
				}
			}
		}
	)
}

data class SaveResult(
	val success: Boolean,
	val message: String
)

fun saveFilesToDirectory(
	sourceDir: String,
	destinationDir: String
): SaveResult {
	return try {
		val source = File(sourceDir)
		if (!source.exists() || !source.isDirectory) {
			return SaveResult(false, "Source directory not found")
		}
		// Create destination with transfer ID folder
		val destination = File(destinationDir)
		if (!destination.exists()) {
			destination.mkdirs()
		}
		// Copy all files
		var copiedCount = 0
		var failedCount = 0

		source.listFiles()?.forEach { file ->
			try {
				val destFile = File(destination, file.name)
				file.copyTo(destFile, overwrite = true)
				copiedCount++
			} catch (e: Exception) {
				failedCount++
				println("Failed to copy ${file.name}: ${e.message}")
			}
		}

		if (failedCount > 0) {
			SaveResult(
				success = true,
				message = "Saved $copiedCount file(s), $failedCount failed"
			)
		} else {
			SaveResult(
				success = true,
				message = "Successfully saved $copiedCount file(s) to:\n${destination.absolutePath}"
			)
		}
	} catch (e: Exception) {
		SaveResult(
			success = false,
			message = "Error: ${e.message}"
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileIconItem(fileName: String, filePath: String) {
	val scope = rememberCoroutineScope()
	val bottomSheetState = LocalBottomSheetScaffoldState.current
	var showWarningDialog by remember { mutableStateOf(false) }

	fun showSnackbar(message: String) {
		scope.launch {
			bottomSheetState.snackbarHostState.showSnackbar(
				message,
				withDismissAction = true
			)
		}
	}

	Card(
		modifier = Modifier.width(80.dp).height(100.dp),
		shape = RoundedCornerShape(8.dp),
		onClick = {
			if (isExecutableFile(fileName)) showWarningDialog = true
			else openFile(filePath) { showSnackbar("File does not exist or cannot be opened.") }
		},
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.surfaceContainerLow.copy(0.5f))
				.padding(8.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Icon(
				imageVector = getFileIcon(fileName),
				contentDescription = null,
				modifier = Modifier.size(32.dp),
				tint = MaterialTheme.colorScheme.primary
			)

			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = fileName,
				style = MaterialTheme.typography.labelSmall,
				maxLines = 1,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.basicMarquee()
			)
		}
	}
	// Warning dialog for executable files
	if (showWarningDialog) {
		AlertDialog(
			onDismissRequest = { showWarningDialog = false },
			icon = {
				Icon(
					imageVector = Icons.Default.Warning,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.error
				)
			},
			title = { Text("Security Warning") },
			text = {
				Text(
					"This file ($fileName) is an executable file and could potentially harm your computer. " +
							"Only open it if you trust the source.\n\n" +
							"Do you want to open this file?"
				)
			},
			confirmButton = {
				TextButton(
					onClick = {
						showWarningDialog = false
						openFile(filePath) { showSnackbar("File does not exist or cannot be opened.") }
					},
					colors = ButtonDefaults.textButtonColors(
						contentColor = MaterialTheme.colorScheme.error
					)
				) {
					Text("Open Anyway")
				}
			},
			dismissButton = {
				TextButton(
					onClick = { showWarningDialog = false }
				) {
					Text("Cancel")
				}
			}
		)
	}
}

fun isExecutableFile(fileName: String): Boolean {
	val dangerousExtensions = listOf(
		// Executable files
		".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".vbe",
		".js", ".jse", ".ws", ".wsf", ".wsh", ".msi", ".msp", ".cpl",
		".jar", ".app", ".deb", ".rpm",
		// Script files
		".ps1", ".psm1", ".psd1", ".ps1xml", ".pssc", ".psc1",
		".sh", ".bash", ".zsh", ".fish",
		// Macro-enabled documents
		".docm", ".xlsm", ".pptm",
		// Other potentially dangerous
		".hta", ".reg", ".inf", ".ins", ".isp", ".lnk", ".msc",
		".gadget", ".application"
	)

	return dangerousExtensions.any { ext ->
		fileName.endsWith(ext, ignoreCase = true)
	}
}

fun openFile(filePath: String, onError: () -> Unit) {
	try {
		val file = File(filePath)
		if (file.exists()) Desktop.getDesktop().open(file)
		else onError()
	} catch (e: Exception) {
		println("Error opening file: ${e.message}")
		e.printStackTrace()
	}
}

val imageExts = setOf(".jpg", ".jpeg", ".png", ".gif")
val documentExts = setOf(".doc", ".docx", ".txt")
val videoExts = setOf(".mp4", ".avi", ".mkv")
val audioExts = setOf(".mp3", ".wav")
val archiveExts = setOf(".zip", ".rar")

private fun getFileIcon(fileName: String): ImageVector {
	return when {
		imageExts.any { fileName.endsWith(it, true) } -> Icons.Default.Image
		documentExts.any { fileName.endsWith(it, true) } -> Icons.Default.Description
		videoExts.any { fileName.endsWith(it, true) } -> Icons.Default.VideoFile
		audioExts.any { fileName.endsWith(it, true) } -> Icons.Default.AudioFile
		archiveExts.any { fileName.endsWith(it, true) } -> Icons.Default.FolderZip
		fileName.endsWith(".pdf", true) -> Icons.Default.PictureAsPdf
		else -> Icons.AutoMirrored.Filled.InsertDriveFile
	}
}

fun timeAgo(epochSeconds: Long): String {
	val now = Instant.now().epochSecond
	val diff = now - epochSeconds

	return when {
		diff < 60 -> "just now"
		diff < 3600 -> "last ${diff / 60} minutes"
		diff < 86400 -> "${diff / 3600} hours ago"
		diff < 2592000 -> "${diff / 86400} days ago"
		else -> "${diff / 2592000} months ago"
	}
}