package com.vincent.transfercloud.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dashedBorder(
	color: Color,
	strokeWidth: Dp,
	cornerRadius: Dp = 0.dp,
	dashLength: Dp = 10.dp,
	gapLength: Dp = 10.dp
): Modifier = this.then(
    Modifier.drawBehind {
        val stroke = Stroke(
	        width = strokeWidth.toPx(),
	        pathEffect = PathEffect.dashPathEffect(
		        floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f
	        )
        )

        drawRoundRect(
            color = color,
            size = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
            style = stroke
        )
    }
)