package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vincent.transfercloud.data.dto.FileOutputDto
import com.vincent.transfercloud.data.dto.FolderOutputDto
import com.vincent.transfercloud.ui.component.panel.FileDetailsTab
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.viewModel.FileDetailVM
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.utils.cursorHand
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.empty_state_multiple_files_selected

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileDetailPanel(
	appState: AppState = koinInject<AppState>(),
	fileDetailVM: FileDetailVM = koinViewModel(),
	folderViewModel: FolderViewModel = koinViewModel()
) {
	val folderData by folderViewModel.folderData.collectAsState()
	val selectedIds by folderViewModel.selectedIds.collectAsState()
	val tempFiles by folderViewModel.tempFiles.collectAsState()
	val summarizeResponse by fileDetailVM.summarizeResponse.collectAsState()
	val uiState by fileDetailVM.uiState.collectAsState()
	val startDestination = FileDetailDestination.DETAIL
	var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
	val singleSelectedItem by remember {
		derivedStateOf {
			if (selectedIds.size == 1) {
				val id = selectedIds.first()
				val folderItem = folderData?.subfolders?.find { it.id == id }
				if (folderItem != null) {
					FolderContentObject.FolderItem(id, folderItem)
				} else {
					val fileItem = folderData?.files?.find { it.id == id }
					fileItem?.let { FolderContentObject.FileItem(id, it) }
				}
			} else {
				null
			}
		}
	}
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes("files/search-files.json").decodeToString()
		)
	}
	var displayedText by remember { mutableStateOf("") }

	LaunchedEffect(summarizeResponse) {
		displayedText = ""
		if (summarizeResponse.isNotEmpty()) {
			val words = summarizeResponse.split(" ")
			val sb = StringBuilder()

			words.forEachIndexed { index, word ->
				sb.append(word).append(" ")
				displayedText = sb.toString()
				delay(50)
			}
		}
	}

	Box(Modifier.fillMaxSize().animateContentSize()) {
		Column(
			Modifier.fillMaxSize().padding(8.dp)
		) {
			Row(
				Modifier.padding(start = 8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				val text = if (selectedIds.size > 1) {
					"${selectedIds.size} items selected"
				} else {
					when (singleSelectedItem) {
						is FolderContentObject.FolderItem -> (singleSelectedItem as FolderContentObject.FolderItem).data.name
						is FolderContentObject.FileItem -> (singleSelectedItem as FolderContentObject.FileItem).data.name
						else -> "File Details"
					}
				}
				Text(
					text,
					style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f)
				)
				Box(
					modifier = Modifier.clip(CircleShape).cursorHand()
						.clickable(
							onClick = { appState.fileDetailShow.value = false },
						)
						.padding(6.dp)
				) {
					Icon(
						Icons.Default.Clear,
						null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.size(24.dp)
					)
				}
			}

			if (selectedIds.isEmpty()) {
				Spacer(Modifier.weight(1f))
				Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
					Image(
						painter = rememberLottiePainter(composition, iterations = 1),
						contentDescription = null,
						modifier = Modifier.widthIn(max = 400.dp, min = 100.dp)
					)
				}

				Text(
					text = "Select any file to see details",
					style = TitleLineBig,
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth(),
				)
				Spacer(Modifier.weight(1f))
				return@Column
			}

			if (selectedIds.size == 1) {
				PrimaryTabRow(
					selectedTabIndex = selectedDestination,
					Modifier.padding(8.dp)
				) {
					FileDetailDestination.entries.forEachIndexed { index, destination ->
						Tab(
							selected = selectedDestination == index,
							onClick = { selectedDestination = index },
							text = {
								Text(
									text = destination.name.lowercase().replaceFirstChar { it.uppercase() },
									style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W600),
									maxLines = 1,
									overflow = TextOverflow.Ellipsis
								)
							}
						)
					}
				}

				when (FileDetailDestination.entries[selectedDestination]) {
					FileDetailDestination.DETAIL -> FileDetailsTab(singleSelectedItem, tempFiles)
					FileDetailDestination.SUMMARIZE -> {
						val item = singleSelectedItem as FolderContentObject.FileItem
						LazyColumn {
							when (uiState) {
								UIState.Ready -> {
									if (summarizeResponse.isNotEmpty()) {
										item {
											Box(
												Modifier.fillMaxWidth()
													.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
													.padding(12.dp)
											) {
												Text(
													displayedText,
													style = MaterialTheme.typography.titleSmall
												)
											}
										}
									}
								}

								UIState.Loading -> {
									item {
										Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
											LoadingIndicator(
												progress = { 0f },
												modifier = Modifier.size(100.dp)
											)
										}
									}
								}

								else -> {
									item {
										Text("Error")
									}
								}
							}
							item {
								ElevatedButton(onClick = { fileDetailVM.requestSummarization(item.id, item.data.ownerId) }) {
									Text("Summarize File Content")
								}
							}
						}
					}
				}

			} else {
				Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
					Image(
						painter = painterResource(Res.drawable.empty_state_multiple_files_selected),
						contentDescription = "Multiselection",
						modifier = Modifier.size(250.dp)
					)
				}
			}
		}
	}
}

enum class FileDetailDestination {
	DETAIL, SUMMARIZE
}

sealed class FolderContentObject {
	data class FolderItem(val id: String, val data: FolderOutputDto) : FolderContentObject()
	data class FileItem(val id: String, val data: FileOutputDto) : FolderContentObject()
}