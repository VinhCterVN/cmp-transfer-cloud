package com.vincent.transfercloud.ui.component.dialog

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.vincent.transfercloud.data.dto.ShareMetadata
import com.vincent.transfercloud.data.enum.SharePermission
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.UIState
import com.vincent.transfercloud.ui.state.getFileIcon
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.ui.viewModel.ShareDialogVM
import com.vincent.transfercloud.utils.cursorHand
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.material_symbols__folder
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ShareFileDialog(
	appState: AppState = koinInject<AppState>(),
	viewModel: ShareDialogVM = koinViewModel(),
	folderViewModel: FolderViewModel = koinViewModel()
) {
	val sharingFolder by appState.sharingFolder.collectAsState()
	val filteredUsers by viewModel.filteredUsers.collectAsState()
	val sharedUsers by viewModel.sharesInfo.collectAsState()
	val uiState by viewModel.uiState.collectAsState()
	var searchQuery by rememberSaveable { mutableStateOf("") }
	var expanded by rememberSaveable { mutableStateOf(false) }
	var itemName by rememberSaveable { mutableStateOf("") }
	var selectedPermission by remember { mutableStateOf(SharePermission.VIEW) }
	val searchBoxHeight by animateDpAsState(
		targetValue = if (expanded && searchQuery.isNotBlank()) 200.dp else 56.dp,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioLowBouncy,
			stiffness = Spring.StiffnessLow
		),
		label = "searchHeight"
	)

	LaunchedEffect(searchQuery) {
		snapshotFlow { searchQuery }
			.onEach {
				if (it.isNotBlank()) viewModel.setUIState(UIState.Loading)
			}
			.debounce(2000)
			.collect { latest ->
				if (latest.isNotBlank()) viewModel.searchUsersByEmail(latest)
				viewModel.setUIState(UIState.Ready)
			}
	}

	LaunchedEffect(sharingFolder) {
		if (sharingFolder.first.isEmpty()) return@LaunchedEffect
		val (id, ownerId, name) = folderViewModel.findItemMetadata(sharingFolder.first, sharingFolder.second)
		itemName = name
		viewModel.getSharesInfo(id, ownerId, sharingFolder.second)
	}

	if (sharingFolder.first.isEmpty()) return

	Dialog(
		onDismissRequest = { appState.sharingFolder.value = "" to false },
		properties = DialogProperties(usePlatformDefaultWidth = false)
	) {
		BoxWithConstraints(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			val maxHeight = this.maxHeight * 0.85f

			Surface(
				modifier = Modifier
					.width(500.dp)
					.heightIn(max = maxHeight)
					.wrapContentHeight()
					.animateContentSize(
						animationSpec = spring(
							dampingRatio = Spring.DampingRatioLowBouncy,
							stiffness = Spring.StiffnessLow
						)
					),
				shape = RoundedCornerShape(12.dp),
				color = MaterialTheme.colorScheme.surface,
				tonalElevation = 6.dp
			) {
				Column(
					modifier = Modifier.fillMaxWidth()
				) {
					Column(
						modifier = Modifier.padding(24.dp)
					) {
						Text(
							text = "Share to user",
							style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.W500),
							modifier = Modifier.padding(bottom = 16.dp)
						)

						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(bottom = 16.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							val resource = if (sharingFolder.second) Res.drawable.material_symbols__folder else getFileIcon(itemName)
							Icon(
								painter = painterResource(resource),
								contentDescription = "File icon",
								modifier = Modifier.size(40.dp),
								tint = MaterialTheme.colorScheme.primary
							)
							Spacer(modifier = Modifier.width(12.dp))
							Text(
								text = itemName,
								style = MaterialTheme.typography.titleMedium,
								maxLines = 1
							)
						}
						Text(
							text = "Permission",
							style = MaterialTheme.typography.labelLarge,
							modifier = Modifier.padding(bottom = 8.dp)
						)
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(bottom = 16.dp),
							horizontalArrangement = Arrangement.spacedBy(8.dp)
						) {
							SharePermission.entries.forEach { permission ->
								FilterChip(
									selected = selectedPermission == permission,
									onClick = { selectedPermission = permission },
									label = { Text(permission.name) },
									elevation = null,
									colors = FilterChipDefaults.filterChipColors(
										selectedContainerColor = when (permission) {
											SharePermission.VIEW -> MaterialTheme.colorScheme.primaryContainer
											SharePermission.EDIT -> MaterialTheme.colorScheme.secondaryContainer
											SharePermission.OWNER -> MaterialTheme.colorScheme.tertiaryContainer
										},
									)
								)
							}
						}

						DockedSearchBar(
							modifier = Modifier
								.fillMaxWidth()
								.height(searchBoxHeight)
								.semantics { traversalIndex = 1f },
							colors = SearchBarDefaults.colors(
								containerColor = MaterialTheme.colorScheme.surfaceVariant
							),
							inputField = {
								SearchBarDefaults.InputField(
									query = searchQuery,
									onQueryChange = { searchQuery = it },
									onSearch = {},
									expanded = expanded,
									onExpandedChange = { expanded = it },
									placeholder = { Text("Enter email to find") },
									modifier = Modifier.fillMaxWidth(),
									leadingIcon = {
										if (uiState == UIState.Loading) {
											CircularProgressIndicator(
												modifier = Modifier.size(16.dp),
												strokeWidth = 2.dp
											)
										} else {
											Icon(Icons.Default.MailOutline, contentDescription = null)
										}
									},
									trailingIcon = {
										if (searchQuery.isNotEmpty()) {
											IconButton(
												onClick = { searchQuery = "" },
												modifier = Modifier.cursorHand()
											) {
												Icon(Icons.Default.Clear, contentDescription = "Clear")
											}
										}
									},
									colors = TextFieldDefaults.colors(
										focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
										unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
									)
								)
							},
							expanded = searchQuery.isNotBlank() && expanded,
							onExpandedChange = { expanded = it },
							shape = RoundedCornerShape(8.dp),
						)
						{
							Box(modifier = Modifier.heightIn(max = 200.dp).wrapContentHeight().animateContentSize()) {
								LazyColumn {
									if (filteredUsers.isEmpty())
										item {
											Text(
												"Không tìm thấy người dùng.",
												modifier = Modifier.padding(16.dp),
												style = MaterialTheme.typography.bodyMedium
											)
										}
									else
										itemsIndexed(filteredUsers) { _, user ->
											ListItem(
												leadingContent = {
													AsyncImage(
														model = user.avatarUrl,
														contentDescription = null,
														contentScale = ContentScale.Crop,
														modifier = Modifier.size(36.dp).clip(CircleShape)
													)
												},
												headlineContent = { Text(user.fullName, style = MaterialTheme.typography.bodyMedium) },
												supportingContent = { Text(user.email, style = MaterialTheme.typography.bodySmall) },
												colors = ListItemDefaults.colors(containerColor = Color.Transparent),
												modifier = Modifier.clickable {
													searchQuery = user.email
													expanded = false
												}
											)
										}
								}
							}
						}
					}

					LazyColumn(
						modifier = Modifier
							.weight(1f, fill = false)
							.fillMaxWidth(),
						contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 8.dp)
					)
					{
						if (sharedUsers.isNotEmpty()) {
							item {
								HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
								Text(
									text = "Shared participants (${sharedUsers.size})",
									style = MaterialTheme.typography.labelLarge,
									modifier = Modifier.padding(bottom = 8.dp),
									color = MaterialTheme.colorScheme.primary
								)
							}
							items(sharedUsers) { user ->
								SharedUserItem(user)
								Spacer(modifier = Modifier.height(8.dp))
							}
						}
					}

					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 24.dp, vertical = 12.dp),
						horizontalArrangement = Arrangement.End,
						verticalAlignment = Alignment.CenterVertically
					) {
						TextButton(onClick = { appState.sharingFolder.value = "" to false }) {
							Text("Cancel")
						}
						Spacer(modifier = Modifier.width(8.dp))
						Button(onClick = { /* Handle confirm */ }) {
							Text("Confirm")
						}
					}
				}
			}
		}
	}
}

@Composable
fun SharedUserItem(user: ShareMetadata) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(8.dp),
		color = MaterialTheme.colorScheme.surfaceContainerLow
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = user.sharedWithUserEmail,
					style = MaterialTheme.typography.bodyMedium,
					maxLines = 1
				)
				val sharedDate = remember(user.sharedAt) {
					try {
						val instant = Instant.parse(user.sharedAt)
						val dateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
						val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
						dateTime.format(formatter)
					} catch (e: Exception) {
						"-"
					}
				}

				Text(
					text = "Shared at: $sharedDate",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			AssistChip(
				onClick = { },
				label = { Text(user.permission.name, style = MaterialTheme.typography.labelSmall) },
				colors = AssistChipDefaults.assistChipColors(
					containerColor = when (user.permission) {
						SharePermission.VIEW -> MaterialTheme.colorScheme.primaryContainer
						SharePermission.EDIT -> MaterialTheme.colorScheme.secondaryContainer
						SharePermission.OWNER -> MaterialTheme.colorScheme.tertiaryContainer
					},
					labelColor = MaterialTheme.colorScheme.onSurface
				),
				border = null
			)
		}
	}
}