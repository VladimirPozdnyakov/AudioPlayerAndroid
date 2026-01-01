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
import com.foxelectronic.audioplayer.FontType
import com.foxelectronic.audioplayer.ui.components.RadioOptionRow

/**
 * Диалог выбора шрифта приложения
 */
@Composable
fun FontSelectionDialog(
    currentFont: FontType,
    onDismiss: () -> Unit,
    onConfirm: (FontType) -> Unit
) {
    var selectedFont by remember { mutableStateOf(currentFont) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите шрифт") },
        text = {
            Column {
                RadioOptionRow(
                    label = "Как в системе",
                    selected = selectedFont == FontType.SYSTEM,
                    onSelect = { selectedFont = FontType.SYSTEM }
                )
                RadioOptionRow(
                    label = "JetBrains Mono (по умолчанию)",
                    selected = selectedFont == FontType.JETBRAINS_MONO,
                    onSelect = { selectedFont = FontType.JETBRAINS_MONO }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedFont)
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
