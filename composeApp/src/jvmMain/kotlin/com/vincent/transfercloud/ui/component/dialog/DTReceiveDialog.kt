package com.vincent.transfercloud.ui.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.vincent.transfercloud.core.server.DirectTransferSend
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.theme.HeadLineMedium
import com.vincent.transfercloud.ui.viewModel.DirectTransferReceiveVM
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReceiveDialog(
	visible: Boolean,
	onDismissRequest: () -> Unit,
	appState: AppState = koinInject<AppState>(),
	viewModel: DirectTransferReceiveVM = koinViewModel()
) {
	if (!visible) return
	// Type = Map<String, DirectTransferSend>
	val receivedFiles by viewModel.receivedData.collectAsState()
	Dialog(onDismissRequest = onDismissRequest) {
		Card(
			shape = RoundedCornerShape(8.dp),
			elevation = CardDefaults.cardElevation(4.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
		) {
			Column(
				Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				Text("Transfer Received Files", style = HeadLineMedium)

				LazyColumn(
					Modifier.heightIn(max = 400.dp).fillMaxWidth().padding(vertical = 8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					items(
						items = receivedFiles.values.toList(),
						key = { it.id }
					) {
						TransferItem(it)
					}
				}
			}
		}
	}
}

@Composable
fun TransferItem(transfer: DirectTransferSend) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		)
	) {
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
				AsyncImage(model = transfer.fromAvatar, contentDescription = null, contentScale = ContentScale.Crop)
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
				Text(
					text = "${timeAgo(transfer.transferTime)} | ${transfer.filesCount} file${if (transfer.filesCount > 1) "s" else ""}",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			// Action button (optional)
			IconButton(onClick = { /* Handle action */ }) {
				Icon(
					imageVector = Icons.Default.MoreVert,
					contentDescription = "More options"
				)
			}
		}
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
