package com.foxelectronic.audioplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun AudioPlayerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentHex: String = "#6750A4",
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val primaryColor = try { Color(android.graphics.Color.parseColor(accentHex)) } catch (_: Throwable) { if (useDark) Color(0xFFCFBCFF) else Color(0xFF6750A4) }
    val scheme = if (useDark) {
        darkColorScheme(primary = primaryColor)
    } else {
        lightColorScheme(primary = primaryColor)
    }
    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}
