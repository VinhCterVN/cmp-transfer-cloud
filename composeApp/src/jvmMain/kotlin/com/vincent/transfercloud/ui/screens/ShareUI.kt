package com.vincent.transfercloud.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.component.fileView.FileChainView
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import com.vincent.transfercloud.ui.state.getFileIcon
import com.vincent.transfercloud.ui.viewModel.ShareViewModel
import com.vincent.transfercloud.ui.viewModel.SharedUiItem
import com.vincent.transfercloud.utils.cursorHand
import com.vincent.transfercloud.utils.detechMouseClick
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareScreen(
	appState: AppState = koinInject<AppState>(),
	viewModel: ShareViewModel = koinViewModel()
) {
	val gridState = rememberLazyGridState()
	val sharedData by viewModel.shareData.collectAsState()
	val uiState by viewModel.uiState.collectAsState()
	LaunchedEffect(Unit) {
		viewModel.getSharedData()
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
				Text(text = (uiState as UIState.Error).message, modifier = Modifier.align(Alignment.Center))
			}

			else -> @Composable {
				Column(Modifier.fillMaxSize().padding(4.dp)) {
					FileChainView()
					Box(Modifier.clip(RoundedCornerShape(12.dp))) {
						LazyVerticalGrid(
							state = gridState,
							columns = GridCells.Adaptive(minSize = 250.dp),
							contentPadding = PaddingValues(8.dp)
						) {
							sharedData.forEach { (timeGroup, items) ->
								item(span = { GridItemSpan(maxLineSpan) }) {
									Text(timeGroup.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
								}

								items(
									items = items,
									key = { it.id }
								) { item ->
									when (item) {
										is SharedUiItem.FolderItem -> {
											Card(
												elevation = CardDefaults.cardElevation(4.dp),
												shape = RoundedCornerShape(12.dp),
												colors = CardDefaults.cardColors(
													containerColor = MaterialTheme.colorScheme.surfaceVariant
												),
												modifier = Modifier.padding(8.dp).aspectRatio(1f)
													.clip(RoundedCornerShape(12.dp)).cursorHand()
													.combinedClickable(
														onClick = {},
														onDoubleClick = {}
													).detechMouseClick(onRightClick = { })
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
															Icons.Default.Folder,
															null,
															tint = MaterialTheme.colorScheme.onSurfaceVariant
														)
														Text(
															item.name,
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
																		onClick = { },
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
														}
													}

													Column(
														Modifier.weight(1f).padding(vertical = 8.dp)
													) {
														Box(
															Modifier.fillMaxSize()
																.clip(RoundedCornerShape(4.dp))
																.background(
																	MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
																	RoundedCornerShape(4.dp)
																)
														) {
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
																	imageVector = Icons.Default.Folder,
																	contentDescription = null,
																	modifier = Modifier.size(48.dp),
																	tint = Color.White.copy(alpha = 0.5f)
																)
															}
														}
													}
													Row(
														modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
														verticalAlignment = Alignment.CenterVertically
													) {
														AsyncImage(
															model = "https://i.pravatar.cc/150?u=User${item.id}",
															contentDescription = null,
															contentScale = ContentScale.Crop,
															modifier = Modifier.size(24.dp).clip(CircleShape)
														)
													}
												}
											}
										}

										is SharedUiItem.FileItem -> {
											Card(
												elevation = CardDefaults.cardElevation(4.dp),
												shape = RoundedCornerShape(12.dp),
												colors = CardDefaults.cardColors(
													containerColor = MaterialTheme.colorScheme.surfaceVariant
												),
												modifier = Modifier.padding(8.dp).aspectRatio(1f)
													.clip(RoundedCornerShape(12.dp)).cursorHand()
													.combinedClickable(
														onClick = {},
														onDoubleClick = {}
													).detechMouseClick(onRightClick = { })
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
															painter = painterResource(getFileIcon(item.data.name)),
															null,
															tint = MaterialTheme.colorScheme.onSurfaceVariant
														)
														Text(
															item.name,
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
																		onClick = { },
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
														}
													}

													Column(
														Modifier.weight(1f).padding(vertical = 8.dp)
													) {
														Box(
															Modifier.fillMaxSize()
																.clip(RoundedCornerShape(4.dp))
																.background(
																	MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
																	RoundedCornerShape(4.dp)
																)
														) {
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
																	painter = painterResource(getFileIcon(item.data.name)),
																	contentDescription = null,
																	modifier = Modifier.size(20.dp),
																	tint = Color.White
																)
															}
														}
													}
													Row(
														modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
														verticalAlignment = Alignment.CenterVertically
													) {
														AsyncImage(
															model = "https://i.pravatar.cc/150?u=User${item.id}",
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
				}
			}
		}
	}
}

