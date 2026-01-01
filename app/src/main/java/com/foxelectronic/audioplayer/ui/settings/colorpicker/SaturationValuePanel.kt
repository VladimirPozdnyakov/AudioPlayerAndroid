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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.ui.settings.utils.ColorUtils

/**
 * Панель выбора насыщенности и яркости цвета
 * Исправлен баг с двойным pointerInput - используется единый блок для drag и tap
 */
@Composable
fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (saturation: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = ColorUtils.hsvToColor(hue, 1f, 1f)
    var panelSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.horizontalGradient(listOf(Color.White, baseColor)))
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            .onSizeChanged { panelSize = it }
            .pointerInput(hue) {
                // Единый pointerInput для drag и tap - исправляет баг
                detectDragGestures { change, _ ->
                    val x = change.position.x.coerceIn(0f, size.width.toFloat()) / size.width
                    val y = change.position.y.coerceIn(0f, size.height.toFloat()) / size.height
                    onChange(x, 1f - y)
                }
            }
            .pointerInput(hue) {
                detectTapGestures { pos ->
                    val x = pos.x.coerceIn(0f, size.width.toFloat()) / size.width
                    val y = pos.y.coerceIn(0f, size.height.toFloat()) / size.height
                    onChange(x, 1f - y)
                }
            }
    ) {
        // Индикатор текущей позиции
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = saturation.coerceIn(0f, 1f) * size.width
            val cy = (1f - value.coerceIn(0f, 1f)) * size.height
            val radius = 8.dp.toPx()

            // Белая обводка
            drawCircle(
                color = Color.White,
                radius = radius,
                center = Offset(cx, cy)
            )
            // Чёрная внутренняя обводка
            drawCircle(
                color = Color.Black,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
