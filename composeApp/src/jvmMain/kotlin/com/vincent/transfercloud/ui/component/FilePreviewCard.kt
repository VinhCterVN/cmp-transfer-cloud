package com.vincent.transfercloud.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.state.getFileIcon
import com.vincent.transfercloud.utils.isAudio
import com.vincent.transfercloud.utils.isImage
import com.vincent.transfercloud.utils.isVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.mdi__file_music
import transfercloud.composeapp.generated.resources.mdi__file_video
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun FilePreviewCard(
	file: File,
	onRemove: () -> Unit
) {
	Column(
		Modifier.widthIn(max = 100.dp)
	) {
		Box(
			modifier = Modifier
				.size(100.dp)
				.clip(RoundedCornerShape(8.dp))
		) {
			when {
				file.isImage() -> {
					// Image preview
					AsyncImage(
						model = file.absolutePath,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxSize()
					)
				}

				file.isVideo() -> {
					Box(Modifier.fillMaxSize()) {
						VideoThumbnail(file)
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
								painter = painterResource(Res.drawable.mdi__file_video),
								contentDescription = null,
								modifier = Modifier.size(20.dp),
								tint = Color.White
							)
						}
					}
				}

				file.isAudio() -> {
					// Audio thumbnail with overlay
					Box(Modifier.fillMaxSize()) {
						AudioThumbnail(file)
						// Music icon overlay
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
								painter = painterResource(Res.drawable.mdi__file_music),
								contentDescription = null,
								modifier = Modifier.size(20.dp),
								tint = Color.White
							)
						}
					}
				}

				else -> {
					// Generic file icon
					Box(
						Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.05f))
					) {
						Icon(
							painterResource(getFileIcon(file.name)),
							contentDescription = null,
							modifier = Modifier
								.fillMaxSize(0.75f)
								.aspectRatio(1f)
								.align(Alignment.Center),
							tint = Color.Unspecified
						)
					}
				}
			}
			// Remove button
			IconButton(
				onClick = onRemove,
				modifier = Modifier
					.size(24.dp)
					.align(Alignment.TopEnd)
			) {
				Icon(
					imageVector = Icons.Default.Close,
					contentDescription = "Remove",
					modifier = Modifier.size(16.dp),
					tint = Color.White
				)
			}
		}

		Text(
			text = file.name,
			maxLines = 1,
			fontSize = 12.sp,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.basicMarquee()
		)
	}
}

@Composable
fun VideoThumbnail(file: File) {
	var thumbnail by remember { mutableStateOf<File?>(null) }

	LaunchedEffect(file) {
		thumbnail = extractVideoThumbnail(file)
	}

	if (thumbnail != null) {
		AsyncImage(
			model = thumbnail!!.absolutePath,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)
	} else {
		// Fallback gradient background
		Box(
			Modifier
				.fillMaxSize()
				.background(
					brush = androidx.compose.ui.graphics.Brush.verticalGradient(
						colors = listOf(
							Color(0xFF1E3A8A),
							Color(0xFF3B82F6)
						)
					)
				),
			contentAlignment = Alignment.Center
		) {
			Icon(
				painter = painterResource(Res.drawable.mdi__file_video),
				contentDescription = null,
				modifier = Modifier.size(48.dp),
				tint = Color.White.copy(alpha = 0.5f)
			)
		}
	}
}

@Composable
fun AudioThumbnail(file: File) {
	var albumArt by remember { mutableStateOf<File?>(null) }

	LaunchedEffect(file) {
		albumArt = extractAudioAlbumArt(file)
	}

	if (albumArt != null) {
		AsyncImage(
			model = albumArt!!.absolutePath,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)
	} else {
		// Fallback gradient background
		Box(
			Modifier
				.fillMaxSize()
				.background(
					brush = androidx.compose.ui.graphics.Brush.verticalGradient(
						colors = listOf(
							Color(0xFF7C3AED),
							Color(0xFFA855F7)
						)
					)
				),
			contentAlignment = Alignment.Center
		) {
			Icon(
				painter = painterResource(Res.drawable.mdi__file_music),
				contentDescription = null,
				modifier = Modifier.size(48.dp),
				tint = Color.White.copy(alpha = 0.5f)
			)
		}
	}
}

suspend fun extractVideoThumbnail(videoFile: File): File? {
	return withContext(Dispatchers.IO) {
		try {
			val thumbnailFile = File.createTempFile("video_thumb_", ".jpg")
			val ffmpegCommand = listOf(
				"ffmpeg",
				"-i", videoFile.absolutePath,
				"-ss", "00:00:01",  // Extract at 1 second
				"-vframes", "1",
				"-q:v", "2",
				thumbnailFile.absolutePath,
				"-y"
			)

			try {
				val process = ProcessBuilder(ffmpegCommand)
					.redirectErrorStream(true)
					.start()

				process.waitFor(5, TimeUnit.SECONDS)

				if (thumbnailFile.exists() && thumbnailFile.length() > 0) {
					return@withContext thumbnailFile
				}
			} catch (e: Exception) {
				println("FFmpeg not available: ${e.message}")
			}
			null
		} catch (e: Exception) {
			println("Error extracting video thumbnail: ${e.message}")
			null
		}
	}
}

suspend fun extractAudioAlbumArt(audioFile: File): File? {
	return withContext(Dispatchers.IO) {
		try {
			val audioFile = org.jaudiotagger.audio.AudioFileIO.read(audioFile)
			val tag = audioFile.tag
			val artwork = tag?.firstArtwork

			if (artwork != null) {
				val thumbnailFile = File.createTempFile("audio_art_", ".${artwork.mimeType.split("/").last()}")
				thumbnailFile.writeBytes(artwork.binaryData)
				return@withContext thumbnailFile
			}

			null
		} catch (e: Exception) {
			println("Error extracting audio album art: ${e.message}")
			null
		}
	}
}