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
import androidx.compose.ui.res.stringResource
import com.foxelectronic.audioplayer.AppTheme
import com.foxelectronic.audioplayer.R
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
        title = { Text(stringResource(R.string.dialog_select_theme)) },
        text = {
            Column {
                RadioOptionRow(
                    label = stringResource(R.string.theme_system),
                    selected = selectedTheme == AppTheme.SYSTEM,
                    onSelect = { selectedTheme = AppTheme.SYSTEM }
                )
                RadioOptionRow(
                    label = stringResource(R.string.theme_light),
                    selected = selectedTheme == AppTheme.LIGHT,
                    onSelect = { selectedTheme = AppTheme.LIGHT }
                )
                RadioOptionRow(
                    label = stringResource(R.string.theme_dark),
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
                Text(stringResource(R.string.btn_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
