package com.vincent.transfercloud.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.vincent.transfercloud.ui.state.UIState
import com.vincent.transfercloud.ui.viewModel.SearchViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class, ExperimentalMaterialApi::class)
@Composable
fun HeadSearchDock(
	viewModel: SearchViewModel = koinViewModel()
) {
	val query by viewModel.query.collectAsState()
	val uiState by viewModel.uiState.collectAsState()
	var expanded by rememberSaveable { mutableStateOf(false) }
	var isFocused by rememberSaveable { mutableStateOf(false) }

	Box {
		DockedSearchBar(
			expanded = expanded,
			onExpandedChange = { expanded = it },
			shape = RoundedCornerShape(8.dp),
			colors = SearchBarDefaults.colors(
				inputFieldColors = TextFieldDefaults.colors(
					focusedContainerColor = MaterialTheme.colorScheme.surface,
					unfocusedContainerColor = MaterialTheme.colorScheme.surface,
					focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
					unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface
				),
			),
			inputField = {
				SearchBarDefaults.InputField(
					query = query,
					onQueryChange = viewModel::setQuery,
					onSearch = { /* Nếu cần thực hiện search thật */ },
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
						if (query.isNotEmpty()) {
							IconButton(
								onClick = viewModel::clearQuery,
								modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
							) {
								Icon(Icons.Default.Close, contentDescription = null)
							}
						}
					},
					colors = TextFieldDefaults.colors(
						unfocusedContainerColor = MaterialTheme.colorScheme.surface,
						focusedContainerColor = MaterialTheme.colorScheme.surface,
					),
					modifier = Modifier
						.widthIn(max = 500.dp)
						.onFocusChanged { isFocused = it.hasFocus },
				)
			},
			content = {
				LazyColumn {
					items(5) {
						ListItem(
							headlineContent = { Text("Search Result $it") },
							leadingContent = {
								Box(
									Modifier
										.size(40.dp)
										.background(
											MaterialTheme.colorScheme.primary,
											RoundedCornerShape(4.dp)
										)
								)
							},
							modifier = Modifier
								.height(48.dp)
								.clickable {
									viewModel.setQuery("Search Result $it")
									expanded = false
								}
						)
					}
				}
			}
		)
	}
}