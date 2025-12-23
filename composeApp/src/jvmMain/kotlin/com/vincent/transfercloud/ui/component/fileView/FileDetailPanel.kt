package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.utils.cursorHand
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.util.zip.ZipOutputStream

@Composable
fun FileDetailPanel(
	appState: AppState = koinInject<AppState>(),
	folderViewModel: FolderViewModel = koinViewModel()
) {
	val selectedId by folderViewModel.selectedIds.collectAsState()

	Box(Modifier.fillMaxSize().animateContentSize()) {
		Column(
			Modifier.fillMaxSize().padding(8.dp)
		) {
			Row(
				Modifier.padding(start = 8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					selectedId.firstOrNull() ?: "No Item Selected",
					style = MaterialTheme.typography.titleMedium,
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
		}
	}
}