package com.vincent.transfercloud.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vincent.transfercloud.ui.component.fileView.FileChainView
import com.vincent.transfercloud.ui.state.LocalBottomSheetScaffoldState
import com.vincent.transfercloud.ui.theme.LabelLineSmall
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import transfercloud.composeapp.generated.resources.Res
import java.awt.datatransfer.DataFlavor
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun FilesArea(
) {
	val listState = rememberLazyGridState()
	val scaffoldState = LocalBottomSheetScaffoldState.current
	val scope = rememberCoroutineScope()
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes("files/empty.json").decodeToString()
		)
	}
	var showTargetBorder by remember { mutableStateOf(false) }
	var targetText by remember { mutableStateOf("Drop Here") }
	val coroutineScope = rememberCoroutineScope()
	val dragAndDropTarget = remember {
		object : DragAndDropTarget {
			override fun onStarted(event: DragAndDropEvent) {
				showTargetBorder = true
			}

			override fun onEnded(event: DragAndDropEvent) {
				showTargetBorder = false
			}

			override fun onDrop(event: DragAndDropEvent): Boolean {
				println("Action at the target: ${event.action}")
				val result = (targetText == "Drop Here") // Example condition
				targetText = event.awtTransferable.let {
					if (it.isDataFlavorSupported(DataFlavor.stringFlavor)) {
						it.getTransferData(DataFlavor.stringFlavor) as String
					} else {
						it.transferDataFlavors.first().humanPresentableName
					}
				}
				coroutineScope.launch {
					delay(2000)
					targetText = "Drop Here"
				}
				return result
			}
		}
	}

	Box(
		Modifier
			.fillMaxSize()
			.dragAndDropTarget(
				shouldStartDragAndDrop = { event ->
					println(event.action)
					true
				},
				target = dragAndDropTarget
			)
	) {
		Column(
			Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp),
		) {
			FileChainView()
			LazyVerticalGrid(
				state = listState,
				columns = GridCells.Adaptive(minSize = 250.dp),
				contentPadding = PaddingValues(8.dp),
				modifier = Modifier.weight(1f)
			) {
				items(15) { index ->
					Card(
						onClick = {
							scope.launch {
								scaffoldState.snackbarHostState.showSnackbar(
									"Hello From SnackBar",
									actionLabel = "Hide",
								)
							}
						},
						colors = CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.surfaceVariant
						),
						elevation = CardDefaults.cardElevation(2.dp),
						shape = RoundedCornerShape(12.dp),
						modifier = Modifier
							.padding(8.dp)
							.height(55.dp)
					) {
						Row(
							Modifier.fillMaxSize().padding(horizontal = 12.dp),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(12.dp)
						) {
							Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
							Column(verticalArrangement = Arrangement.Center) {
								Text(
									"A new File $index", style = TextStyle(
										fontWeight = FontWeight.SemiBold,
										fontSize = 16.sp
									)
								)

								Text(
									"Last seen",
									style = LabelLineSmall.copy(fontWeight = FontWeight.Normal, fontSize = 14.sp)
								)
							}
							Spacer(Modifier.weight(1f))

							Box(
								modifier = Modifier
									.clip(CircleShape)
									.pointerHoverIcon(PointerIcon.Hand)
									.clickable {}
									.padding(4.dp)
							) {
								Icon(
									Icons.Default.MoreVert,
									null,
									tint = MaterialTheme.colorScheme.onSurfaceVariant,
									modifier = Modifier.size(18.dp)
								)
							}
						}
					}
				}
			}
		}

		VerticalScrollbar(
			modifier = Modifier.align(Alignment.CenterEnd)
				.fillMaxHeight()
				.width(6.dp),
			adapter = rememberScrollbarAdapter(listState),
		)
	}
}

@OptIn(ExperimentalTime::class)
fun Long.toDateString(): String {
	val instant = Instant.fromEpochMilliseconds(this)
	val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
	val dayOfWeek = localDateTime.dayOfWeek.name.take(3).lowercase()
		.replaceFirstChar { it.uppercase() }
	val day = localDateTime.day
	val month = localDateTime.month.name.take(3).lowercase()
		.replaceFirstChar { it.uppercase() }

	return "$dayOfWeek $day $month"
}
