package com.vincent.transfercloud.ui.component.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.component.fileView.FolderContentObject
import com.vincent.transfercloud.ui.state.getFileIcon
import org.jetbrains.compose.resources.painterResource
import java.io.File

@Composable
fun FileDetailsTab(singleSelectedItem: FolderContentObject?, tempFiles: Map<String, File>) {
	Box(
		Modifier.fillMaxWidth().padding(8.dp).aspectRatio(1f)
			.clip(RoundedCornerShape(12.dp)),
		contentAlignment = Alignment.Center
	) {
		when (singleSelectedItem) {
			is FolderContentObject.FolderItem -> {
				Icon(
					Icons.Default.Folder, null,
					modifier = Modifier.fillMaxWidth(0.85f).aspectRatio(1f),
					tint = MaterialTheme.colorScheme.primary
				)
			}

			is FolderContentObject.FileItem -> {
				val file = singleSelectedItem
				if (file.data.hasThumbnail && tempFiles[file.data.id]?.exists() == true) {
					AsyncImage(
						model = tempFiles[file.data.id]?.absolutePath,
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
							painter = painterResource(getFileIcon(file.data.name)),
							contentDescription = null,
							modifier = Modifier.size(20.dp),
							tint = Color.White
						)
					}
				} else {
					Box(
						Modifier
							.fillMaxSize()
							.background(
								brush = Brush.verticalGradient(
									colors = listOf(
										Color(0xFF1E3A8A).copy(0.25f),
										Color(0xFF3B82F6).copy(0.25f)
									)
								)
							),
						contentAlignment = Alignment.Center
					) {
						Icon(
							painter = painterResource(getFileIcon(file.data.name)),
							contentDescription = null,
							modifier = Modifier.size(48.dp),
							tint = Color.White.copy(alpha = 0.5f)
						)
					}
				}

			}

			else -> {}
		}
	}

	Text(
		"People with access",
		style = MaterialTheme.typography.titleMedium,
		modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
	)

	LazyRow(
		Modifier.fillMaxWidth().padding(bottom = 8.dp)
	) {
		item { Spacer(Modifier.width(8.dp)) }
		items(10) {
			AsyncImage(
				model = "https://i.pravatar.cc/150?u=User$it",
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier.size(48.dp).clip(CircleShape)
			)
			Spacer(Modifier.width(8.dp))
		}
	}

	HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

}