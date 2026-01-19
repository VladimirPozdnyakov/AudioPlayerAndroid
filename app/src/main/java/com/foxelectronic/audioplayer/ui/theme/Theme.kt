package com.foxelectronic.audioplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.FontType
import com.foxelectronic.audioplayer.R

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Расширенные цвета для современного дизайна
 */
data class ExtendedColors(
    val cardBackground: Color,
    val cardBackgroundElevated: Color,
    val cardBorder: Color,
    val subtleText: Color,
    val iconTint: Color,
    val accentSoft: Color,
    val divider: Color,
    val shimmer: Color
)

/**
 * Расширенные размеры для единообразия дизайна
 */
data class ExtendedDimens(
    val cardCornerRadius: Dp = 16.dp,
    val cardCornerRadiusSmall: Dp = 12.dp,
    val cardElevation: Dp = 2.dp,
    val cardPadding: Dp = 16.dp,
    val cardPaddingSmall: Dp = 12.dp,
    val iconSize: Dp = 24.dp,
    val iconSizeLarge: Dp = 28.dp,
    val itemSpacing: Dp = 12.dp,
    val sectionSpacing: Dp = 24.dp
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        cardBackground = Color.Unspecified,
        cardBackgroundElevated = Color.Unspecified,
        cardBorder = Color.Unspecified,
        subtleText = Color.Unspecified,
        iconTint = Color.Unspecified,
        accentSoft = Color.Unspecified,
        divider = Color.Unspecified,
        shimmer = Color.Unspecified
    )
}

val LocalExtendedDimens = staticCompositionLocalOf { ExtendedDimens() }

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

    // Создаём мягкий акцентный цвет с низкой альфой
    val accentSoft = primaryColor.copy(alpha = if (useDark) 0.15f else 0.08f)

    val scheme = if (useDark) {
        darkColorScheme(
            primary = primaryColor,
            surface = Color(0xFF121212),
            surfaceVariant = Color(0xFF1E1E1E),
            background = Color(0xFF0A0A0A),
            onSurface = Color(0xFFE8E8E8),
            onSurfaceVariant = Color(0xFFB0B0B0),
            outline = Color(0xFF2A2A2A),
            outlineVariant = Color(0xFF1F1F1F)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            surface = Color(0xFFFDFDFD),
            surfaceVariant = Color(0xFFF5F5F5),
            background = Color(0xFFF8F8F8),
            onSurface = Color(0xFF1A1A1A),
            onSurfaceVariant = Color(0xFF666666),
            outline = Color(0xFFE5E5E5),
            outlineVariant = Color(0xFFEEEEEE)
        )
    }

    val extendedColors = if (useDark) {
        ExtendedColors(
            cardBackground = Color(0xFF1A1A1A),
            cardBackgroundElevated = Color(0xFF222222),
            cardBorder = Color(0xFF2D2D2D),
            subtleText = Color(0xFF888888),
            iconTint = Color(0xFFB0B0B0),
            accentSoft = accentSoft,
            divider = Color(0xFF262626),
            shimmer = Color(0xFF2A2A2A)
        )
    } else {
        ExtendedColors(
            cardBackground = Color.White,
            cardBackgroundElevated = Color(0xFFFAFAFA),
            cardBorder = Color(0xFFEAEAEA),
            subtleText = Color(0xFF999999),
            iconTint = Color(0xFF666666),
            accentSoft = accentSoft,
            divider = Color(0xFFF0F0F0),
            shimmer = Color(0xFFF5F5F5)
        )
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
                FontFamily.Default
            }
        }
        FontType.SYSTEM -> FontFamily.Default
    }

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(24.dp)
    )

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalExtendedDimens provides ExtendedDimens()
    ) {
        MaterialTheme(
            colorScheme = scheme,
            shapes = shapes,
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
}

/**
 * Расширение для доступа к ExtendedColors через MaterialTheme
 */
object AudioPlayerThemeExtended {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current

    val dimens: ExtendedDimens
        @Composable
        get() = LocalExtendedDimens.current
}
