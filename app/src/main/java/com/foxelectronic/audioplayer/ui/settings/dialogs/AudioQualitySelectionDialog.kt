package com.foxelectronic.audioplayer.ui.settings.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.AudioQualityPreference
import com.foxelectronic.audioplayer.R

/**
 * Диалог выбора качества аудио
 */
@Composable
fun AudioQualitySelectionDialog(
    currentQuality: AudioQualityPreference,
    onDismiss: () -> Unit,
    onConfirm: (AudioQualityPreference) -> Unit
) {
    var selectedQuality by remember { mutableStateOf(currentQuality) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.audio_quality_preference)) },
        text = {
            Column {
                AudioQualityPreference.entries.forEach { quality ->
                    val (title, description) = when (quality) {
                        AudioQualityPreference.AUTO ->
                            stringResource(R.string.audio_quality_auto) to stringResource(R.string.audio_quality_auto_desc)
                        AudioQualityPreference.PRIORITIZE_QUALITY ->
                            stringResource(R.string.audio_quality_high) to stringResource(R.string.audio_quality_high_desc)
                        AudioQualityPreference.SAVE_BANDWIDTH ->
                            stringResource(R.string.audio_quality_save) to stringResource(R.string.audio_quality_save_desc)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedQuality = quality }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = selectedQuality == quality,
                            onClick = { selectedQuality = quality }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedQuality) }) {
                Text(stringResource(R.string.btn_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
