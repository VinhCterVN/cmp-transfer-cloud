package com.vincent.transfercloud.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vincent.transfercloud.ui.navigation.FolderDetailView
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.theme.HeadLineMedium
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUI(
	appState: AppState = koinInject<AppState>(),
) {
	val navigator = LocalNavigator.currentOrThrow
	val currentUser by appState.currentUser.collectAsState()
	Column(
		Modifier.fillMaxSize()
			.pointerInput(Unit) {
				awaitPointerEventScope {
					var lastItem: Screen? = null
					while (true) {
						val event = awaitPointerEvent()
						if (event.buttons.isBackPressed && navigator.canPop) {
							lastItem = navigator.lastItemOrNull
							navigator.pop()
						} else if (event.buttons.isForwardPressed) {
							if (lastItem != null) navigator.push(lastItem)
						}
					}
				}
			}
			.padding(horizontal = 8.dp, vertical = 4.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text("Welcome to TransferCloud!", style = HeadLineMedium)
		Spacer(Modifier.height(10.dp))

		HorizontalMultiBrowseCarousel(
			state = rememberCarouselState { 20 },
			modifier = Modifier
				.fillMaxWidth()
				.wrapContentHeight()
				.padding(top = 16.dp, bottom = 16.dp),
			preferredItemWidth = 186.dp,
			itemSpacing = 8.dp,
			contentPadding = PaddingValues(horizontal = 16.dp)
		) { i ->
			AsyncImage(
				model = "https://i.pravatar.cc/300?u=${i}",
				contentDescription = null,
				modifier = Modifier
					.height(205.dp)
					.maskClip(MaterialTheme.shapes.extraLarge),
				contentScale = ContentScale.Crop
			)
		}

		ElevatedButton({
			navigator.push(FolderDetailView(currentUser?.rootFolderId!!))
		}) {
			Text("Go to your Drive")
		}
	}
}