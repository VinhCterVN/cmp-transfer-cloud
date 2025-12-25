package com.vincent.transfercloud.ui.component.dialog

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.vincent.transfercloud.utils.cursorHand

@Composable
fun FileOptionMenu(
	expanded: Boolean,
	offset: DpOffset = DpOffset.Zero,
	onDismissRequest:  () -> Unit,
	onRename: () -> Unit,
	onShare: () -> Unit,
	onMove: () -> Unit,
	onDownload: () -> Unit,
	onDelete: () -> Unit
) {
	DropdownMenu(
		expanded = expanded,
		offset = offset,
		onDismissRequest = onDismissRequest,
		modifier = Modifier.widthIn(min = 200.dp),
		shape = RoundedCornerShape(8.dp)
	) {
		DropdownMenuItem(
			text = { Text("Rename") },
			onClick = onRename,
			leadingIcon = {
				Icon(Icons.Default.Edit, contentDescription = null)
			},
			modifier = Modifier.cursorHand()
		)
		DropdownMenuItem(
			text = { Text("Share") },
			onClick = onShare,
			leadingIcon = {
				Icon(Icons.Default.Share, contentDescription = null)
			},
			modifier = Modifier.cursorHand()
		)
		DropdownMenuItem(
			text = { Text("Move") },
			onClick = onMove,
			leadingIcon = {
				Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null)
			},
			modifier = Modifier.cursorHand()
		)
		DropdownMenuItem(
			text = { Text("Download") },
			onClick = onDownload,
			leadingIcon = {
				Icon(Icons.Rounded.Download, contentDescription = null)
			},
			modifier = Modifier.cursorHand()
		)
		HorizontalDivider()
		DropdownMenuItem(
			text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
			onClick = onDelete,
			leadingIcon = {
				Icon(
					Icons.Default.Delete,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.error
				)
			},
			modifier = Modifier.cursorHand()
		)
	}

}