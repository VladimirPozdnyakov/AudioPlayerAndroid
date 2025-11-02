package com.foxelectronic.audioplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.foxelectronic.audioplayer.FontType
import com.foxelectronic.audioplayer.R

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun AudioPlayerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentHex: String = "#6750A4",
    fontType: FontType = FontType.JETBRAINS_MONO,
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

    // Define font family based on selection
    val fontFamily = when (fontType) {
        FontType.JETBRAINS_MONO -> {
            try {
                FontFamily(
                    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
                    Font(R.font.jetbrains_mono_italic, FontWeight.Normal, FontStyle.Italic),
                    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
                )
            } catch (e: Exception) {
                // Fallback to default font if JetBrains Mono is not available
                FontFamily.Default
            }
        }
        FontType.SYSTEM -> FontFamily.Default
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = fontFamily),
            displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = fontFamily),
            displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = fontFamily),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = fontFamily),
            titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = fontFamily),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = fontFamily),
            labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = fontFamily),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = fontFamily),
        ),
        content = content
    )
}
