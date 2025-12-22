package com.vincent.transfercloud.ui.component.dialog

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.state.UIState
import com.vincent.transfercloud.ui.state.getFileIcon
import com.vincent.transfercloud.ui.theme.LabelLineMedium
import com.vincent.transfercloud.ui.theme.MessageStyle
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.utils.cursorHand
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.material_symbols__folder
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class, ExperimentalMaterialApi::class)
@Composable
fun FileUploadDialog(
	uploadFile: File?,
	viewModel: FolderViewModel = koinInject<FolderViewModel>(),
	onCancel: () -> Unit,
	action: () -> Unit = {}
) {
	val options = listOf("Local" to Icons.Default.LocalBar, "Cloud" to Icons.Default.Cloud)
	var selectedOption by remember { mutableStateOf(options[0]) }
	val filteredUsers by viewModel.filteredUsers.collectAsState()
	val isFile = uploadFile?.isDirectory == false
	var shareEmail by remember { mutableStateOf("") }
	val sharedUsers = remember { mutableStateListOf<String>() }
	var dropdownExpanded by remember { mutableStateOf(false) }
	var expanded by rememberSaveable { mutableStateOf(false) }
	var uiState by remember { mutableStateOf<UIState>(UIState.Ready) }

	LaunchedEffect(shareEmail) {
		snapshotFlow { shareEmail }
			.onEach {
				if (it.isNotBlank()) uiState = UIState.Loading
			}
			.debounce(2000)
			.collect { latest ->
				if (latest.isNotBlank()) {
					viewModel.searchUsersByEmail(latest)
				}
				uiState = UIState.Ready
			}
	}
	val searchBoxHeight by animateDpAsState(
		targetValue = if (expanded && shareEmail.isNotBlank()) 200.dp else 56.dp,
		animationSpec = tween(
			durationMillis = 300
		),
	)

	if (uploadFile != null) {
		Dialog(onDismissRequest = onCancel) {
			Card(
//				modifier = Modifier.wrapContentSize(),
				shape = RoundedCornerShape(8.dp),
				elevation = CardDefaults.cardElevation(4.dp),
				colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
			) {
				Column(
					Modifier.padding(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(6.dp)
				) {
					Text(
						"Upload ${if (isFile) "file" else "folder"} ${uploadFile.name}",
						style = TitleLineLarge,
						modifier = Modifier.align(Alignment.Start)
					)
					Spacer(Modifier.height(6.dp))
					Card(
						Modifier.fillMaxWidth().heightIn(max = 50.dp),
						colors = CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.surfaceVariant
						),
						shape = RoundedCornerShape(8.dp),
						elevation = CardDefaults.cardElevation(2.dp)
					) {
						Row(
							Modifier.fillMaxSize().padding(8.dp),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(8.dp)
						) {
							Image(
								painter = painterResource(if (isFile) getFileIcon(uploadFile.name) else Res.drawable.material_symbols__folder),
								contentDescription = null,
								modifier = Modifier.size(36.dp),
							)
							Text(uploadFile.name, style = MessageStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
							Spacer(Modifier.weight(1f))
							Text(formatFileSize(uploadFile.length()), style = MessageStyle, maxLines = 1)
						}
					}
					Spacer(Modifier.height(6.dp))
					Text(
						"Storage Location",
						modifier = Modifier.align(Alignment.Start),
						style = LabelLineMedium.copy(fontWeight = FontWeight.W500)
					)
					ExposedDropdownMenuBox(
						expanded = dropdownExpanded,
						onExpandedChange = { dropdownExpanded = !dropdownExpanded },
						modifier = Modifier.fillMaxWidth()
					) {
						OutlinedTextField(
							value = selectedOption.first,
							onValueChange = {},
							readOnly = true,
							label = { Text("Location") },
							leadingIcon = { Icon(selectedOption.second, null) },
							textStyle = TitleLineLarge.copy(fontWeight = FontWeight.W500),
							trailingIcon = {
								ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
							},
							shape = RoundedCornerShape(8.dp),
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
						)

						ExposedDropdownMenu(
							expanded = dropdownExpanded,
							onDismissRequest = { dropdownExpanded = false }
						) {
							options.forEach { option ->
								DropdownMenuItem(
									text = { Text(option.first) },
									leadingIcon = { Icon(option.second, null) },
									onClick = {
										selectedOption = option
										expanded = false
									}
								)
							}
						}
					}

					Spacer(Modifier.height(6.dp))
					Text("Shares", modifier = Modifier.align(Alignment.Start), style = LabelLineMedium.copy(fontWeight = FontWeight.W500))
					DockedSearchBar(
						modifier = Modifier.fillMaxWidth().height(searchBoxHeight).semantics { traversalIndex = 1f },
						inputField = {
							SearchBarDefaults.InputField(
								query = shareEmail,
								onQueryChange = { shareEmail = it },
								onSearch = {},
								expanded = expanded,
								onExpandedChange = { expanded = it },
								placeholder = { Text("Search") },
								leadingIcon = {
									if (uiState == UIState.Loading) CircularProgressIndicator(
										gapSize = 0.dp,
										strokeCap = StrokeCap.Round,
										modifier = Modifier.size(24.dp)
									)
									else Icon(Icons.Default.Person, null)
								},
								trailingIcon = {
									if (shareEmail.isNotEmpty()) {
										IconButton(
											onClick = { shareEmail = ""; expanded = false },
											modifier = Modifier.cursorHand()
										) {
											Icon(
												imageVector = Icons.Default.Close,
												contentDescription = null,
											)
										}
									}
								},
								modifier = Modifier.fillMaxWidth()
							)
						},
						expanded = shareEmail.isNotBlank() && expanded,
						onExpandedChange = { expanded = it },
						shape = RoundedCornerShape(8.dp),
						colors = SearchBarDefaults.colors(
							inputFieldColors = TextFieldDefaults.colors(
								focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
								unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface
							),
						),
					) {
						Box(
							modifier = Modifier.height(150.dp)
								.wrapContentHeight()
						) {
							LazyColumn(Modifier.wrapContentHeight()) {
								if (filteredUsers.isEmpty()) {
									item {
										Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
											Text("No users found.", style = TitleLineLarge)
										}
									}
								}
								itemsIndexed(filteredUsers) { index, user ->
									ListItem(
										leadingContent = {
											AsyncImage(
												model = user.avatarUrl,
												contentDescription = null,
												contentScale = ContentScale.Crop,
												modifier = Modifier.size(36.dp).clip(CircleShape)
											)
										},
										headlineContent = {
											Text(user.fullName, style = TitleLineLarge)
										},
										supportingContent = {
											Text(user.email, style = LabelLineMedium)
										},
										trailingContent = {
											TextButton(
												onClick = {
													if (!sharedUsers.contains(user.id)) {
														sharedUsers.add(user.id)
													} else {
														sharedUsers.remove(user.id)
													}
												}
											) {
												Text(
													if (sharedUsers.contains(user.email)) "Remove" else "Add",
													style = TitleLineLarge.copy(fontSize = 14.sp)
												)
											}
										},
										colors = ListItemDefaults.colors(containerColor = Color.Transparent),
									)
								}
							}
						}
					}

					if (sharedUsers.isNotEmpty()) {
						LazyRow(
							modifier = Modifier.fillMaxWidth().heightIn(max = 100.dp),
							horizontalArrangement = Arrangement.spacedBy(4.dp),
						) {
							itemsIndexed(filteredUsers) { index, user ->
								if (!sharedUsers.contains(user.id)) return@itemsIndexed
								AssistChip(
									onClick = { sharedUsers.remove(user.id) },
									label = {
										Text(user.email, style = TitleLineLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.W500))
									},
									leadingIcon = {
										Icon(Icons.Default.Person, null)
									},
								)
							}
						}
					}
				}
				Row(
					modifier = Modifier.align(Alignment.End).padding(vertical = 8.dp, horizontal = 16.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(4.dp)
				) {
					TextButton(onCancel) {
						Text("Close", style = TitleLineLarge.copy(fontSize = 15.sp))
					}
					Button(
						onClick = {
							viewModel.uploadFile(uploadFile, sharedUsers)
							action()
						},
						shape = RoundedCornerShape(8.dp)
					) {
						Text("Confirm Upload", style = TitleLineLarge.copy(fontSize = 15.sp))
					}
				}
			}
		}
	}
}

fun formatFileSize(length: Long): String {
	return when {
		length > 1024 * 1024 * 1024 -> String.format("%.2f GB", length.toFloat() / (1024 * 1024 * 1024))
		length > 1024 * 1024 -> String.format("%.2f MB", length.toFloat() / (1024 * 1024))
		length > 1024 -> String.format("%.2f KB", length.toFloat() / 1024)
		else -> "$length B"
	}
}