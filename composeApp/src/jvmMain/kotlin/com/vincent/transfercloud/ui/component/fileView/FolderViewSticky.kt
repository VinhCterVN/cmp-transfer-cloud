package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.folderViewSticky(
	showSticky: Boolean,
	count: Int,
	onAction: () -> Unit
) {
	if (showSticky) {
		stickyHeader {
			Surface(
				tonalElevation = 2.dp,
				shadowElevation = 2.dp,
				modifier = Modifier
					.fillMaxWidth()
					.clip(CircleShape)
			) {
				Row(
					modifier = Modifier.fillMaxSize(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(4.dp)
				) {
					TooltipBox(
						positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
						tooltip = {
							PlainTooltip { Text("Clear selected") }
						},
						state = rememberTooltipState()
					) {
						IconButton(onClick = onAction) {
							Icon(Icons.Default.Clear, null)
						}
					}

					Text(
						"Selected $count item${if (count != 1) "s" else ""}",
						style = MaterialTheme.typography.titleMedium
					)

					TooltipBox(
						positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
						tooltip = {
							PlainTooltip { Text("Share with anyone") }
						},
						state = rememberTooltipState()
					) {
						IconButton({}) {
							Icon(Icons.Default.PersonAdd, null, Modifier.size(20.dp))
						}
					}

					Spacer(Modifier.weight(1f))

					Row(
						modifier = Modifier.padding(end = 4.dp),
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						TextButton(onClick = { /* TODO: bulk actions like download/share */ }) {
							Text("Actions")
						}
						TextButton(onClick = onAction) {
							Text("Clear")
						}
					}
				}
			}
		}
	}
}