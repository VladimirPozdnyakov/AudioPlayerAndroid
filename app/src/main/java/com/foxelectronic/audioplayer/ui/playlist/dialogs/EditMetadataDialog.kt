package com.foxelectronic.audioplayer.ui.playlist.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.foxelectronic.audioplayer.data.model.Track

/**
 * Диалог редактирования метаданных трека
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedCoverUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать информацию") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Обложка
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") }
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
                            contentDescription = "Обложка",
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Выбрать",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Поля ввода
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Исполнитель") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Альбом") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Чекбокс для записи в файл
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = writeToFile,
                        onCheckedChange = { writeToFile = it }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Записать в файл",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Изменить ID3 теги в самом файле",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        val newTitle = title.trim().takeIf { it != track.title }
                        val newArtist = artist.trim().takeIf { it != (track.artist ?: "") }
                        val newAlbum = album.trim().takeIf { it != (track.album ?: "") }

                        android.util.Log.d("EditMetadataDialog", "Saving: title=$newTitle, artist=$newArtist, album=$newAlbum")
                        android.util.Log.d("EditMetadataDialog", "Original track: title=${track.title}, artist=${track.artist}, album=${track.album}")

                        onSave(newTitle, newArtist, newAlbum, selectedCoverUri, writeToFile) {
                            // Колбэк вызывается после завершения сохранения
                            android.util.Log.d("EditMetadataDialog", "Save completed, closing dialog")
                            onDismiss()
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Сохранить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
