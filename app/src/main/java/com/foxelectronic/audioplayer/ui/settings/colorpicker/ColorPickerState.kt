package com.foxelectronic.audioplayer.ui.settings.colorpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.foxelectronic.audioplayer.ui.settings.utils.ColorUtils

/**
 * State holder для color picker
 * Управляет HSV значениями и их преобразованием в Color
 */
@Stable
class ColorPickerState(initialColor: Color) {
    private val hsv = ColorUtils.colorToHsv(initialColor)

    var hue by mutableStateOf(hsv[0])
    var saturation by mutableStateOf(hsv[1])
    var value by mutableStateOf(hsv[2])

    val currentColor: Color
        get() = ColorUtils.hsvToColor(hue, saturation, value)

    fun updateSaturationValue(s: Float, v: Float) {
        saturation = s.coerceIn(0f, 1f)
        value = v.coerceIn(0f, 1f)
    }

    fun updateHue(h: Float) {
        hue = h.coerceIn(0f, 360f)
    }

    fun reset() {
        val defaultColor = ColorUtils.parseColorSafe(ColorUtils.DEFAULT_ACCENT)
        val defaultHsv = ColorUtils.colorToHsv(defaultColor)
        hue = defaultHsv[0]
        saturation = defaultHsv[1]
        value = defaultHsv[2]
    }
}

@Composable
fun rememberColorPickerState(initialColor: Color): ColorPickerState {
    return remember(initialColor) { ColorPickerState(initialColor) }
}
