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
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.AppTheme
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
    onThemeClick: () -> Unit,
    onAccentClick: () -> Unit,
    onFontClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Настройка темы
        val themeLabel = when (currentTheme) {
            AppTheme.SYSTEM -> "Системная"
            AppTheme.LIGHT -> "Светлая"
            AppTheme.DARK -> "Тёмная"
        }
        SettingsListItem(
            title = "Тема",
            subtitle = themeLabel,
            onClick = onThemeClick
        )

        // Настройка акцентного цвета
        val accentName = PresetAccent.getDisplayName(currentAccentHex)
        SettingsListItem(
            title = "Акцентный цвет",
            subtitle = accentName,
            trailing = {
                val color = ColorUtils.parseColorSafe(currentAccentHex)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color)
                        .semantics { contentDescription = "Превью акцентного цвета" }
                )
            },
            onClick = onAccentClick
        )

        // Настройка шрифта
        val fontLabel = when (currentFont) {
            FontType.SYSTEM -> "Как в системе"
            FontType.JETBRAINS_MONO -> "JetBrains Mono"
        }
        SettingsListItem(
            title = "Шрифт",
            subtitle = fontLabel,
            onClick = onFontClick
        )
    }
}
