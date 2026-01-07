package com.foxelectronic.audioplayer.ui.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.AppLanguage
import com.foxelectronic.audioplayer.AppTheme
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.FontType
import com.foxelectronic.audioplayer.ui.components.SettingsListItem
import com.foxelectronic.audioplayer.ui.settings.utils.ColorUtils
import com.foxelectronic.audioplayer.ui.settings.utils.PresetAccent

/**
 * Секция настроек интерфейса
 * Использует Column вместо LazyColumn (только 3 элемента - Material3 best practice)
 */
@Composable
fun InterfaceSection(
    currentTheme: AppTheme,
    currentAccentHex: String,
    currentFont: FontType,
    currentLanguage: AppLanguage,
    onThemeClick: () -> Unit,
    onAccentClick: () -> Unit,
    onFontClick: () -> Unit,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Настройка темы
        val themeLabel = when (currentTheme) {
            AppTheme.SYSTEM -> stringResource(R.string.theme_system)
            AppTheme.LIGHT -> stringResource(R.string.theme_light)
            AppTheme.DARK -> stringResource(R.string.theme_dark)
        }
        SettingsListItem(
            title = stringResource(R.string.settings_theme),
            subtitle = themeLabel,
            onClick = onThemeClick
        )

        // Настройка акцентного цвета
        val accentName = PresetAccent.getDisplayName(currentAccentHex)
        val accentColorPreviewDesc = stringResource(R.string.accent_color_preview)
        SettingsListItem(
            title = stringResource(R.string.settings_accent),
            subtitle = accentName,
            trailing = {
                val color = ColorUtils.parseColorSafe(currentAccentHex)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color)
                        .semantics { contentDescription = accentColorPreviewDesc }
                )
            },
            onClick = onAccentClick
        )

        // Настройка шрифта
        val fontLabel = when (currentFont) {
            FontType.SYSTEM -> stringResource(R.string.font_system)
            FontType.JETBRAINS_MONO -> stringResource(R.string.font_jetbrains)
        }
        SettingsListItem(
            title = stringResource(R.string.settings_font),
            subtitle = fontLabel,
            onClick = onFontClick
        )

        // Настройка языка
        val languageLabel = when (currentLanguage) {
            AppLanguage.ENGLISH -> stringResource(R.string.language_english)
            AppLanguage.RUSSIAN -> stringResource(R.string.language_russian)
        }
        SettingsListItem(
            title = stringResource(R.string.settings_language),
            subtitle = languageLabel,
            onClick = onLanguageClick
        )
    }
}
