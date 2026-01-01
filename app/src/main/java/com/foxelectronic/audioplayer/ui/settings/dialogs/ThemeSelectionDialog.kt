package com.foxelectronic.audioplayer.ui.settings.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.foxelectronic.audioplayer.AppTheme
import com.foxelectronic.audioplayer.ui.components.RadioOptionRow

/**
 * Диалог выбора темы приложения
 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onConfirm: (AppTheme) -> Unit
) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите тему") },
        text = {
            Column {
                RadioOptionRow(
                    label = "Системная",
                    selected = selectedTheme == AppTheme.SYSTEM,
                    onSelect = { selectedTheme = AppTheme.SYSTEM }
                )
                RadioOptionRow(
                    label = "Светлая",
                    selected = selectedTheme == AppTheme.LIGHT,
                    onSelect = { selectedTheme = AppTheme.LIGHT }
                )
                RadioOptionRow(
                    label = "Тёмная",
                    selected = selectedTheme == AppTheme.DARK,
                    onSelect = { selectedTheme = AppTheme.DARK }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedTheme)
                    onDismiss()
                }
            ) {
                Text("Готово")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
