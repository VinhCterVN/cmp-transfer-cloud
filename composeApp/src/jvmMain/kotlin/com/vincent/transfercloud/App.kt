package com.vincent.transfercloud

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import com.vincent.transfercloud.ui.component.auth.AuthDialog
import com.vincent.transfercloud.ui.component.HeaderBar
import com.vincent.transfercloud.ui.component.SideBar
import com.vincent.transfercloud.ui.navigation.FileView
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.AppTheme
import com.vincent.transfercloud.ui.theme.HeadLineLarge
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import org.koin.compose.koinInject
import java.awt.Cursor


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
@Composable
fun App(
	appState: AppState = koinInject<AppState>()
) {
	val currentUser by appState.currentUser.collectAsState()
	val scaffoldState = LocalBottomSheetScaffoldState.current
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
	AppTheme(darkTheme = false) {
		BottomSheetScaffold(
			scaffoldState = scaffoldState,
			sheetPeekHeight = 0.dp,
			containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(0.5f),
			modifier = Modifier.blur(if (currentUser == null) 8.dp else 0.dp),
			sheetContent = @Composable {
				Column(
					Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text("Sheet Content", style = HeadLineLarge)
				}
			},
			snackbarHost = { SnackbarHost(hostState = scaffoldState.snackbarHostState) }
		) { innerPadding ->
			Column(
				Modifier.padding(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				HeaderBar()

				HorizontalSplitPane(
					splitPaneState = splitterState,
				) {
					first(150.dp) {
						Box(Modifier.fillMaxSize().padding(8.dp)) {
							SideBar()
						}
					}

					second(400.dp) {
						Card(
							Modifier.fillMaxSize().padding(8.dp),
							shape = RoundedCornerShape(12.dp),
							elevation = CardDefaults.cardElevation(2.dp),
							colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
						) {
							Navigator(FileView())
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
		AuthDialog()
	}
}