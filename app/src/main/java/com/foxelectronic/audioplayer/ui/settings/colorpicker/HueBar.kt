package com.foxelectronic.audioplayer.ui.settings.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

/**
 * Полоса выбора оттенка (Hue)
 */
@Composable
fun HueBar(
    hue: Float,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val hueGradientColors = listOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color.Cyan,
        Color.Blue,
        Color.Magenta,
        Color.Red
    )

    var heightPx by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier.onSizeChanged { heightPx = it.height.toFloat() }
    ) {
        // Основная полоса оттенков
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.small)
                .background(Brush.verticalGradient(hueGradientColors))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val h = (change.position.y / size.height).coerceIn(0f, 1f) * 360f
                        onChange(h)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { pos ->
                        val h = (pos.y / size.height).coerceIn(0f, 1f) * 360f
                        onChange(h)
                    }
                }
        )

        // Индикатор текущего значения
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = (hue / 360f) * size.height
            val strokeWidth = 4.dp.toPx()

            // Чёрная обводка
            drawLine(
                color = Color.Black,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth + 2.dp.toPx()
            )
            // Белая линия
            drawLine(
                color = Color.White,
                start = Offset(2.dp.toPx(), y),
                end = Offset(size.width - 2.dp.toPx(), y),
                strokeWidth = strokeWidth
            )
        }
    }
}
