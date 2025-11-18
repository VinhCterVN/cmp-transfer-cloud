package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.FileViewIndex
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileChainView(
	appState: AppState = koinInject<AppState>()
) {
	val navigator = LocalNavigator.currentOrThrow
	val chain by appState.breadcrumb.collectAsState()
	val viewIndex by appState.currentViewIndex.collectAsState()
	val options = listOf(Icons.AutoMirrored.Filled.List, Icons.Default.GridView)


	Row(
		Modifier.fillMaxWidth().padding(4.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		LazyRow(
			Modifier.weight(1f),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start
		) {
			item {
				IconButton({ navigator.pop() }) {
					Icon(Icons.Default.ChevronLeft, null)
				}
			}

			itemsIndexed(chain) { index, file ->
				TextButton(
					onClick = { if (index != chain.size - 1) navigator.push(FolderDetailView(file.id)) else null },
					contentPadding = ButtonDefaults.SmallContentPadding,
					interactionSource = null,
					modifier = Modifier.padding(0.dp)

				) {
					Text(
						file.name,
						style = TitleLineLarge.copy(
							fontWeight = FontWeight.W600
						),
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
				if (index < chain.size - 1) {
					Icon(Icons.Default.ChevronRight, null)
				}
			}
		}

		SingleChoiceSegmentedButtonRow {
			options.forEachIndexed { index, icon ->
				SegmentedButton(
					onClick = { appState.currentViewIndex.value = if (index > 0) FileViewIndex.GRID else FileViewIndex.LIST },
					selected = viewIndex == if (index > 0) FileViewIndex.GRID else FileViewIndex.LIST,
					label = { Icon(icon, null) },
					colors = SegmentedButtonDefaults.colors(
						activeContainerColor = MaterialTheme.colorScheme.primaryContainer
					),
					shape = SegmentedButtonDefaults.itemShape(
						index = index,
						count = options.size
					),
				)
			}
		}
	}
}