package com.foxelectronic.audioplayer.ui.playlist.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.data.model.Track
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Современный диалог редактирования метаданных трека
 */
@Composable
fun EditMetadataDialog(
    track: Track,
    onDismiss: () -> Unit,
    onSave: (title: String?, artist: String?, album: String?, coverUri: Uri?, writeToFile: Boolean, onComplete: () -> Unit) -> Unit
) {
    var title by remember { mutableStateOf(track.title) }
    var artist by remember { mutableStateOf(track.artist ?: "") }
    var album by remember { mutableStateOf(track.album ?: "") }
    var selectedCoverUri by remember { mutableStateOf<Uri?>(null) }
    var writeToFile by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val extendedColors = AudioPlayerThemeExtended.colors

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedCoverUri = uri
    }

    ModernDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = stringResource(R.string.dialog_edit_metadata),
        confirmText = stringResource(R.string.btn_save),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            if (!isSaving) {
                isSaving = true
                val newTitle = title.trim().takeIf { it != track.title }
                val newArtist = artist.trim().takeIf { it != (track.artist ?: "") }
                val newAlbum = album.trim().takeIf { it != (track.album ?: "") }

                onSave(newTitle, newArtist, newAlbum, selectedCoverUri, writeToFile) {
                    onDismiss()
                }
            }
        },
        onDismiss = { if (!isSaving) onDismiss() },
        confirmEnabled = !isSaving
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Индикатор сохранения
            if (isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(R.string.btn_save) + "...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extendedColors.subtleText
                        )
                    }
                }
            }

            // Обложка
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .clickable(enabled = !isSaving) { imagePickerLauncher.launch("image/*") }
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                val coverToShow = selectedCoverUri?.toString() ?: track.albumArtPath
                if (coverToShow != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(coverToShow)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.field_cover),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = extendedColors.subtleText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.btn_select),
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.subtleText
                        )
                    }
                }
            }

            // Поля ввода
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.field_title)) },
                singleLine = true,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text(stringResource(R.string.field_artist)) },
                supportingText = {
                    Text(
                        text = stringResource(R.string.artist_separator_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.subtleText
                    )
                },
                singleLine = true,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = album,
                onValueChange = { album = it },
                label = { Text(stringResource(R.string.field_album)) },
                singleLine = true,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            // Чекбокс для записи в файл
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .clickable(enabled = !isSaving) { writeToFile = !writeToFile }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = writeToFile,
                    onCheckedChange = { writeToFile = it },
                    enabled = !isSaving
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = stringResource(R.string.write_to_file),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.write_id3_tags),
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.subtleText
                    )
                }
            }
        }
    }
}
