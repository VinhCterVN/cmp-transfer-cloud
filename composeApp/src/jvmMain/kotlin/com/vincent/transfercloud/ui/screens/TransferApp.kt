package com.vincent.transfercloud.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import com.vincent.transfercloud.ui.component.HeaderBar
import com.vincent.transfercloud.ui.component.SideBar
import com.vincent.transfercloud.ui.navigation.HomeScreen
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.awt.Cursor

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TransferApp() {
	val splitterState = rememberSplitPaneState()
	var isHovered by remember { mutableStateOf(false) }
	val bgColor by animateColorAsState(
		targetValue = if (isHovered) Color(0xFFAAAAAA) else Color(0x00000000),
		animationSpec = tween(
			durationMillis = 300,
			easing = EaseOut
		),
		label = "hoverColor"
	)

	Column(
		Modifier.background(
			Brush.linearGradient(
				colors = listOf(
					MaterialTheme.colorScheme.primaryContainer.copy(0.45f),
					MaterialTheme.colorScheme.secondaryContainer.copy(0.45f),
				),
				start = Offset.Zero,
				end = Offset.Infinite
			)
		).padding(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		HeaderBar()

		HorizontalSplitPane(
			splitPaneState = splitterState,
		) {
			first(250.dp) {
				Box(Modifier.fillMaxSize().padding(8.dp)) {
					SideBar()
				}
			}

			second(400.dp) {
				Card(
					Modifier.fillMaxSize().padding(8.dp),
					shape = RoundedCornerShape(12.dp),
					elevation = CardDefaults.cardElevation(2.dp),
					colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
				) {
					Navigator(HomeScreen())
				}
			}

			splitter {
				visiblePart {
					Box(
						Modifier.width(1.dp)
							.fillMaxHeight()
					)
				}
				handle {
					Box(
						Modifier
							.fillMaxHeight()
							.width(4.dp)
							.padding(vertical = 12.dp)
							.background(bgColor, CircleShape)
							.onPointerEvent(PointerEventType.Enter) { isHovered = true }
							.onPointerEvent(PointerEventType.Exit) { isHovered = false }
							.pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
							.markAsHandle()
					)
				}
			}
		}
	}

}