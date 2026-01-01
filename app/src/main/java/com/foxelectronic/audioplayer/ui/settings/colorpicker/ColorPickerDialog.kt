package com.foxelectronic.audioplayer.ui.settings.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.ui.settings.utils.ColorUtils

/**
 * Диалог выбора акцентного цвета
 * Использует HSV color picker с исправленными багами
 */
@Composable
fun ColorPickerDialog(
    currentAccentHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialColor = ColorUtils.parseColorSafe(currentAccentHex)
    val pickerState = rememberColorPickerState(initialColor)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите цвет") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Панель выбора цвета
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SaturationValuePanel(
                        hue = pickerState.hue,
                        saturation = pickerState.saturation,
                        value = pickerState.value,
                        onChange = pickerState::updateSaturationValue,
                        modifier = Modifier
                            .size(180.dp)
                            .semantics { contentDescription = "Панель выбора цвета" }
                    )
                    HueBar(
                        hue = pickerState.hue,
                        onChange = pickerState::updateHue,
                        modifier = Modifier
                            .height(180.dp)
                            .width(24.dp)
                            .semantics { contentDescription = "Полоса выбора оттенка" }
                    )
                }

                // Превью и HEX код
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(initialColor)
                            .semantics { contentDescription = "Исходный цвет" }
                    )
                    Text("→", style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(pickerState.currentColor)
                            .semantics { contentDescription = "Новый цвет" }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = ColorUtils.colorToHex(pickerState.currentColor),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Кнопка сброса
                TextButton(
                    onClick = { pickerState.reset() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сбросить на стандартный")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hex = ColorUtils.colorToHex(pickerState.currentColor)
                    onConfirm(hex)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
