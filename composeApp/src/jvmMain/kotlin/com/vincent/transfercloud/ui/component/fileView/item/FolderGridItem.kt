package com.vincent.transfercloud.ui.component.fileView.item

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vincent.transfercloud.data.dto.FolderOutputDto
import com.vincent.transfercloud.ui.component.dialog.FileOptionMenu
import com.vincent.transfercloud.ui.viewModel.FolderObject
import com.vincent.transfercloud.utils.cursorHand
import com.vincent.transfercloud.utils.detechMouseClick
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun FolderGridItem(
	// Data & State
	index: Int,
	folder: FolderOutputDto,
	isSelected: Boolean,
	isHovered: Boolean,
	isMenuExpanded: Boolean,
	draggedItem: Pair<String, FolderObject>?, // State từ VM
	windowInfo: WindowInfo, // Cần truyền từ LocalWindowInfo hoặc context cha
	// Actions / Callbacks
	onNavigate: (String) -> Unit, // Navigator push
	onToggleSelection: (String, Boolean) -> Unit, // id, isCtrlPressed
	onMenuToggle: (Boolean) -> Unit, // Open/Close menu
	// Drag & Drop Interactions
	onDragStart: (Offset) -> DragAndDropTransferData,
	onDragEnd: () -> Unit,
	onHoverChanged: (Boolean) -> Unit, // true = entered, false = exited
	onDropItem: (DragAndDropEvent) -> Boolean, // Return true nếu drop thành công
	// Menu Options
	onShare: () -> Unit,
	onDownload: () -> Unit,
	onDelete: () -> Unit,
	onRename: () -> Unit = {}, // Placeholder nếu cần
	onMove: () -> Unit = {}   // Placeholder nếu cần
) {
	// Animation States
	var hasAppeared by rememberSaveable(folder.id) { mutableStateOf(false) }
	val animatedProgress = remember(folder.id) {
		Animatable(initialValue = if (hasAppeared) 1f else 0f)
	}
	// Drag & Drop Target Logic
	val dragAndDropTarget = remember(folder.id, onHoverChanged, onDropItem) {
		object : DragAndDropTarget {
			override fun onEntered(event: DragAndDropEvent) {
				onHoverChanged(true)
			}

			override fun onExited(event: DragAndDropEvent) {
				onHoverChanged(false)
			}

			override fun onDrop(event: DragAndDropEvent): Boolean {
				onHoverChanged(false)
				return onDropItem(event)
			}
		}
	}
	// Entry Animation
	LaunchedEffect(folder.id) {
		if (!hasAppeared) {
			delay((index % 10) * 50L)
			animatedProgress.animateTo(
				targetValue = 1f,
				animationSpec = tween(300)
			)
			hasAppeared = true
		}
	}
	// UI Styles
	val borderColor = MaterialTheme.colorScheme.primary // Giả định màu border
	val containerColor = when {
		isHovered || isSelected -> MaterialTheme.colorScheme.primaryContainer
		else -> MaterialTheme.colorScheme.surfaceVariant
	}

	Card(
		shape = RoundedCornerShape(12.dp),
		elevation = CardDefaults.cardElevation(4.dp),
		colors = CardDefaults.cardColors(containerColor = containerColor),
		modifier = Modifier
			.padding(8.dp)
			.height(55.dp)
			.clip(RoundedCornerShape(12.dp))
			.cursorHand()
			.graphicsLayer {
				alpha = animatedProgress.value
				val scale = 0.8f + (0.2f * animatedProgress.value)
				scaleX = scale
				scaleY = scale
			}
			.drawWithContent {
				drawContent()
				if (isHovered) {
					drawRoundRect(
						color = borderColor,
						size = size,
						cornerRadius = CornerRadius(12.dp.toPx()),
						style = Stroke(width = 2.dp.toPx())
					)
				}
			}
			.dragAndDropSource { offset -> onDragStart(offset) }
			.dragAndDropTarget(
				shouldStartDragAndDrop = { _ ->
					// Chỉ nhận drop nếu item đang drag không phải là chính folder này
					draggedItem != null && draggedItem.first != folder.id
				},
				target = dragAndDropTarget
			)
			.combinedClickable(
				onClick = {
					val modifiers = windowInfo.keyboardModifiers
					val isCtrlPressed = modifiers.isCtrlPressed || modifiers.isMetaPressed
					onToggleSelection(folder.id, isCtrlPressed)
				},
				onDoubleClick = { onNavigate(folder.id) },
			)
			.detechMouseClick(onRightClick = { onMenuToggle(true) })
	) {
		Row(
			Modifier
				.fillMaxSize()
				.padding(horizontal = 12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

			Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
				Text(
					folder.name,
					style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
				)
				// Text(folder.createdAt, ...) // Uncomment nếu cần
			}

			Spacer(Modifier.weight(1f))
			// Menu Box
			Box {
				Box(
					modifier = Modifier
						.clip(CircleShape)
						.cursorHand()
						.clickable { onMenuToggle(true) }
						.padding(4.dp)
				) {
					Icon(
						Icons.Default.MoreVert,
						null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.size(18.dp)
					)
				}

				FileOptionMenu(
					expanded = isMenuExpanded,
					onDismissRequest = { onMenuToggle(false) },
					onRename = {
						onMenuToggle(false)
						onRename()
					},
					onMove = {
						onMenuToggle(false)
						onMove()
					},
					onShare = {
						onMenuToggle(false)
						onShare()
					},
					onDownload = {
						onMenuToggle(false)
						onDownload()
					},
					onDelete = {
						onMenuToggle(false)
						onDelete()
					}
				)
			}
		}
	}
}