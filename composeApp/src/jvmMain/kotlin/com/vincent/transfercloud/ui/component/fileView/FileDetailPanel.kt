package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.state.getFileIcon
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.viewModel.FileDetailVM
import com.vincent.transfercloud.ui.viewModel.FolderViewModel
import com.vincent.transfercloud.utils.cursorHand
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.empty_state_multiple_files_selected
import kotlin.random.Random

@Composable
fun FileDetailPanel(
	appState: AppState = koinInject<AppState>(),
	fileDetailVM: FileDetailVM = koinViewModel(),
	folderViewModel: FolderViewModel = koinViewModel()
) {
	val selectedIds by folderViewModel.selectedIds.collectAsState()
	val tempFiles by folderViewModel.tempFiles.collectAsState()
	val composition by rememberLottieComposition {
		LottieCompositionSpec.JsonString(
			Res.readBytes("files/search-files.json").decodeToString()
		)
	}
	Box(Modifier.fillMaxSize().animateContentSize()) {
		Column(
			Modifier.fillMaxSize().padding(8.dp)
		) {
			Row(
				Modifier.padding(start = 8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				val text = if (selectedIds.size > 1) {
					"${selectedIds.size} items selected"
				} else {
					selectedIds.firstOrNull() ?: "No item selected"
				}
				Text(
					text,
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

			if (selectedIds.isEmpty()) {
				Spacer(Modifier.weight(1f))
				Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
					Image(
						painter = rememberLottiePainter(composition, iterations = 1),
						contentDescription = null,
						modifier = Modifier.widthIn(max = 400.dp, min = 100.dp)
					)
				}

				Text(
					text = "Select any file to see details",
					style = TitleLineBig,
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth(),
				)
				Spacer(Modifier.weight(1f))
				return@Column
			}

			if (selectedIds.size == 1) {
				PrimaryTabRow(
					selectedTabIndex = 0,
					Modifier.padding(8.dp)
				) {
					Tab(
						selected = Random.nextBoolean(),
						onClick = {},
						text = {
							Text(
								text = "Details",
								style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W600),
								maxLines = 1,
								overflow = TextOverflow.Ellipsis
							)
						}
					)
					Tab(
						selected = Random.nextBoolean(),
						onClick = {},
						text = {
							Text(
								text = "Activities",
								style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W600),
								maxLines = 1,
								overflow = TextOverflow.Ellipsis
							)
						}
					)
				}

				Card(
					modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp),
					shape = RoundedCornerShape(12.dp),
					elevation = CardDefaults.cardElevation(4.dp),
				) {
					Box() {
						AsyncImage(
							model = tempFiles.values.first().absolutePath,
							contentDescription = null,
							contentScale = ContentScale.Crop,
							modifier = Modifier.fillMaxSize(),
						)

						Box(
							modifier = Modifier
								.align(Alignment.BottomEnd)
								.padding(4.dp)
								.background(
									Color.Black.copy(alpha = 0.6f),
									RoundedCornerShape(4.dp)
								)
								.padding(4.dp)
						) {
							Icon(
								painter = painterResource(getFileIcon("Music.mp3")),
								contentDescription = null,
								modifier = Modifier.size(20.dp),
								tint = Color.White
							)
						}
					}
				}
			} else {
				Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
					Image(
						painter = painterResource(Res.drawable.empty_state_multiple_files_selected),
						contentDescription = "Multiselection",
						modifier = Modifier.size(250.dp)
					)
				}
			}
		}
	}
}