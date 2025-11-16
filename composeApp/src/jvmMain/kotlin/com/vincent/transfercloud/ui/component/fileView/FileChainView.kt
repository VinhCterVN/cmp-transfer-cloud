package com.vincent.transfercloud.ui.component.fileView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vincent.transfercloud.ui.state.AppState
import com.vincent.transfercloud.ui.theme.HeadLineMedium
import com.vincent.transfercloud.ui.theme.TitleLineBig
import com.vincent.transfercloud.ui.theme.TitleLineLarge
import org.koin.compose.koinInject

@Composable
fun FileChainView(
	appState: AppState = koinInject<AppState>()
) {
	val chain = listOf("My Drive", "Projects", "Transfer")

	LazyRow(
		Modifier.fillMaxWidth().padding(horizontal = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Start
	) {
		itemsIndexed(chain) { index, file ->
			TextButton(
				onClick = {}
			) {
				Text(
					file,
					style = TitleLineLarge.copy(
						fontWeight = FontWeight.W600
					)
				)
			}
			if (index < chain.size - 1) {
				Icon(Icons.AutoMirrored.Filled.ArrowRight, null)
			}
		}
	}
}