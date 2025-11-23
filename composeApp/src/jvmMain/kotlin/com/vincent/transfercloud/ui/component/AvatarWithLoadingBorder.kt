package com.vincent.transfercloud.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.state.AppState
import org.koin.compose.koinInject

@Composable
fun ConnectivityAvatar(
	imageUrl: String,
	onClick: () -> Unit,
	appState: AppState = koinInject<AppState>()
) {
	val isConnected by appState.isConnected.collectAsState()
	val infiniteTransition = rememberInfiniteTransition()
	val angle by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = 360f,
		animationSpec = infiniteRepeatable(
			animation = tween(1000, easing = LinearEasing)
		)
	)
	val showLoadingInfo = !isConnected
	val borderBrush = if (showLoadingInfo) {
		Brush.sweepGradient(listOf(Color.Transparent, Color.Blue, Color.Cyan))
	} else {
		SolidColor(MaterialTheme.colorScheme.primary)
	}
	val rotation = if (showLoadingInfo) angle else 0f

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.size(150.dp)
			.aspectRatio(1f)
			.clip(CircleShape)
			.clickable(onClick = onClick)
	) {
		Spacer(
			modifier = Modifier
				.matchParentSize()
				.rotate(rotation)
				.border(
					width = 1.5.dp,
					brush = borderBrush,
					shape = CircleShape
				)
		)
		// --- LAYER 2: AVATAR ---
		// Ảnh tĩnh, không xoay theo border
		AsyncImage(
			model = imageUrl,
			contentDescription = "Avatar",
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.matchParentSize()
				.padding(3.dp) // Khoảng cách giữa ảnh và border
				.clip(CircleShape)
		)
	}
}