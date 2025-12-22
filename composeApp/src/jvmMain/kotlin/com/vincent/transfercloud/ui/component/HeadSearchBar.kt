package com.vincent.transfercloud.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.vincent.transfercloud.ui.theme.MessageStyle

@Composable
fun HeadSearchBar() {
	var query by rememberSaveable { mutableStateOf("") }
	var isFocused by rememberSaveable { mutableStateOf(false) }


	BasicTextField(
		value = query,
		onValueChange = { query = it },
		singleLine = true,
		modifier = Modifier.widthIn(max = 500.dp).onFocusChanged { isFocused = it.hasFocus },
		textStyle = MessageStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
	) { innerTextField ->
		Row(
			Modifier.background(
				if (isFocused) MaterialTheme.colorScheme.primaryContainer.copy(0.75f) else MaterialTheme.colorScheme.surface,
				CircleShape
			).padding(vertical = 0.dp, horizontal = 12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

			Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
				if (query.isEmpty()) {
					Text(
						text = "Search file", style = LocalTextStyle.current.copy(
							color = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.5f),
						), maxLines = 1
					)
				}
				innerTextField()
			}
			if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
				Icon(Icons.Default.Clear, null, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand))
			}
			else IconButton(onClick = { }) {}
		}
	}
}