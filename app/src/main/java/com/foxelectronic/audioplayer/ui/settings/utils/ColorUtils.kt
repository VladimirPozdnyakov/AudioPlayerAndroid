package com.foxelectronic.audioplayer.ui.settings.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import android.graphics.Color as AndroidColor

/**
 * Централизованное управление цветами для настроек
 */
object ColorUtils {
    const val DEFAULT_ACCENT = "#B498FF"

    /**
     * Безопасный парсинг HEX цвета с fallback
     */
    fun parseColorSafe(hex: String, fallback: Color = Color.Magenta): Color {
        return try {
            Color(AndroidColor.parseColor(hex))
        } catch (e: Throwable) {
            fallback
        }
    }

    /**
     * Конвертация Color в HEX строку
     */
    fun colorToHex(color: Color): String {
        return String.format("#%06X", (color.toArgb() and 0xFFFFFF))
    }

    /**
     * Конвертация HSV в Color
     */
    fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
        return Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value)))
    }

    /**
     * Конвертация Color в HSV
     */
    fun colorToHsv(color: Color): FloatArray {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(color.toArgb(), hsv)
        return hsv
    }

    /**
     * Определяет, является ли цвет тёмным (для выбора контрастного текста/иконки)
     */
    fun isColorDark(color: Color): Boolean {
        val argb = color.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        // Формула относительной яркости
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
        return luminance < 0.5
    }
}

/**
 * Предопределенные акцентные цвета
 */
enum class PresetAccent(val hex: String, val displayName: String) {
    PURPLE("#6750A4", "Фиолетовый"),
    BLUE("#1E88E5", "Синий"),
    GREEN("#2E7D32", "Зелёный"),
    RED("#E53935", "Красный"),
    ORANGE("#FB8C00", "Оранжевый"),
    PINK("#D81B60", "Розовый");

    companion object {
        fun fromHex(hex: String): PresetAccent? {
            return values().find { it.hex.equals(hex, ignoreCase = true) }
        }

        fun getDisplayName(hex: String): String {
            return fromHex(hex)?.displayName ?: hex.uppercase()
        }
    }
}
