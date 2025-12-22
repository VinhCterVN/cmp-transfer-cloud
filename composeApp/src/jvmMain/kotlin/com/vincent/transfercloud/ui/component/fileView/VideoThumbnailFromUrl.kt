package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import transfercloud.composeapp.generated.resources.Res
import transfercloud.composeapp.generated.resources.mdi__file_video
import java.io.File

@Composable
fun VideoThumbnailFromUrl(videoUrl: String) {
	var thumbnail by remember { mutableStateOf<String?>(null) }
	var isLoading by remember { mutableStateOf(true) }

	LaunchedEffect(videoUrl) {
		thumbnail = extractVideoThumbnailFromUrl(videoUrl)
		isLoading = false
	}

	Box(Modifier.fillMaxSize()) {
		when {
			isLoading -> {
				CircularProgressIndicator(
					modifier = Modifier
						.size(24.dp)
						.align(Alignment.Center)
				)
			}
			thumbnail != null -> {
				AsyncImage(
					model = thumbnail,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)
			}
			else -> {
				// Fallback gradient
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

		// Video icon overlay
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

suspend fun extractVideoThumbnailFromUrl(videoUrl: String): String? {
	return withContext(Dispatchers.IO) {
		try {
			// Method 1: Download file tạm và extract
			val tempVideoFile = File.createTempFile("temp_video_", ".mp4")
			val thumbnailFile = File.createTempFile("video_thumb_", ".jpg")

			// Download video (chỉ cần một phần nhỏ)
			downloadPartialFile(videoUrl, tempVideoFile, maxBytes = 5 * 1024 * 1024) // 5MB

			// Extract thumbnail using FFmpeg
			val ffmpegCommand = listOf(
				"ffmpeg",
				"-i", tempVideoFile.absolutePath,
				"-ss", "00:00:01",
				"-vframes", "1",
				"-q:v", "2",
				thumbnailFile.absolutePath,
				"-y"
			)

			val process = ProcessBuilder(ffmpegCommand)
				.redirectErrorStream(true)
				.start()

			process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)

			// Clean up temp video
			tempVideoFile.delete()

			if (thumbnailFile.exists() && thumbnailFile.length() > 0) {
				return@withContext thumbnailFile.absolutePath
			}

			null
		} catch (e: Exception) {
			println("Error extracting video thumbnail from URL: ${e.message}")
			null
		}
	}
}

// Download một phần file để tiết kiệm bandwidth
suspend fun downloadPartialFile(url: String, outputFile: File, maxBytes: Long) {
	withContext(Dispatchers.IO) {
		try {
			val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
			connection.requestMethod = "GET"
			connection.setRequestProperty("Range", "bytes=0-$maxBytes")
			connection.connect()

			connection.inputStream.use { input ->
				outputFile.outputStream().use { output ->
					input.copyTo(output)
				}
			}
		} catch (e: Exception) {
			println("Error downloading partial file: ${e.message}")
		}
	}
}