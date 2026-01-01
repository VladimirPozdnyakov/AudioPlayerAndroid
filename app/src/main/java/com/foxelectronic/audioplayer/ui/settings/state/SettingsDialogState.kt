package com.foxelectronic.audioplayer.ui.settings.state

import com.foxelectronic.audioplayer.AppTheme
import com.foxelectronic.audioplayer.FontType

/**
 * Sealed class для управления состоянием диалогов
 * Заменяет 4 отдельные Boolean переменные, обеспечивает type-safety
 */
sealed class SettingsDialogState {
    /** Нет активного диалога */
    object None : SettingsDialogState()

    /** Диалог выбора темы */
    data class ThemeSelection(val currentTheme: AppTheme) : SettingsDialogState()

    /** Диалог выбора акцентного цвета */
    data class AccentSelection(val currentAccentHex: String) : SettingsDialogState()

    /** Диалог выбора шрифта */
    data class FontSelection(val currentFont: FontType) : SettingsDialogState()

    /** Диалог подтверждения удаления папки */
    data class FolderDeletion(val folderUri: String, val folderName: String) : SettingsDialogState()
}
