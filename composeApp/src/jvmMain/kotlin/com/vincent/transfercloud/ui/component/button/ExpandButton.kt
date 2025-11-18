package com.vincent.transfercloud.ui.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExpandButton(
	state: MutableState<Boolean>,
	text: String
) {
	Row(
		modifier = Modifier
			.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		TextButton(
			onClick = { state.value = !state.value },
			contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
			shape = CircleShape
		) {
			Icon(
				if (state.value) Icons.Default.KeyboardArrowDown
				else Icons.AutoMirrored.Filled.KeyboardArrowRight,
				contentDescription = null,
				modifier = Modifier.size(24.dp),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text,
				style = TextStyle(
					fontWeight = FontWeight.Bold,
					fontSize = 16.sp,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			)
		}
	}

}