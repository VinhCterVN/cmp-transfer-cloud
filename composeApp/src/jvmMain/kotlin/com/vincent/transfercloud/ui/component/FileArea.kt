package com.vincent.transfercloud.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.*
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.viewModel.AppViewModel
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import org.koin.compose.koinInject
import transfercloud.composeapp.generated.resources.Res

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileLargePanel(
	appState: AppState = koinInject<AppState>(),
	viewModel: AppViewModel = koinInject<AppViewModel>()
) {

	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes("files/empty.json").decodeToString()
		)
	}
	var expanded by remember { mutableStateOf(false) }
	val focusRequester = FocusRequester()

	val items = listOf(
		FloatingMenuItem(Icons.Outlined.Edit, "Compose") {
			appState.isComposing.value = true
		},
		FloatingMenuItem(Icons.Filled.People, "Reply all") {},
		FloatingMenuItem(Icons.Filled.SelectAll, "Select") {},
		FloatingMenuItem(Icons.AutoMirrored.Filled.Label, "Label") {},
	)

		Box(
			Modifier.fillMaxSize()
		) {
			FilesArea()
			FloatingActionButtonMenu(
				expanded = expanded,
				modifier = Modifier.align(Alignment.BottomEnd),
				button = {
					ToggleFloatingActionButton(
						modifier = Modifier.semantics {
							traversalIndex = -1f
							stateDescription = if (expanded) "Expanded" else "Collapsed"
							contentDescription = "Toggle Menu"
						}.animateFloatingActionButton(
							visible = true,
							alignment = Alignment.BottomEnd
						).focusRequester(focusRequester = focusRequester),
						checked = expanded,
						onCheckedChange = { expanded = !expanded },
					) {
						val imageVector by remember {
							derivedStateOf { if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add }
						}

						Icon(
							painter = rememberVectorPainter(imageVector),
							contentDescription = null,
							modifier = Modifier.animateIcon({ checkedProgress })
						)
					}
				}
			) {
				items.forEachIndexed { i, item ->
					FloatingActionButtonMenuItem(
						modifier = Modifier.semantics {
							isTraversalGroup = true
							if (i == items.size - 1) {
								customActions = listOf(
									CustomAccessibilityAction(
										label = "Close Menu",
										action = {
											expanded = false
											true
										}
									))
							}
						}.then(
							if (i == 0) {
								Modifier.onKeyEvent {
									if (
										it.type == KeyEventType.KeyDown &&
										(it.key == Key.DirectionUp ||
												(it.isShiftPressed && it.key == Key.Tab))
									) {
										focusRequester.requestFocus()
										return@onKeyEvent true
									}
									return@onKeyEvent false
								}
							} else {
								Modifier
							}
						),
						onClick = item.onClick,
						icon = { Icon(item.icon, contentDescription = null) },
						text = { Text(text = item.text) }
					)
				}
			}
		}
}

data class FloatingMenuItem(
	val icon: ImageVector,
	val text: String,
	val onClick: () -> Unit
)