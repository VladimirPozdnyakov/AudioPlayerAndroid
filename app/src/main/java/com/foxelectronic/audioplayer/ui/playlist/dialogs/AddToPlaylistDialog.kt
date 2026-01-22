package com.foxelectronic.audioplayer.ui.playlist.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.data.model.PlaylistWithTrackCount
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.components.ModernSelectionItem
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Современный диалог добавления трека в плейлист
 * @param playlists список всех плейлистов
 * @param trackId ID трека, который добавляем
 * @param playlistsContainingTrack множество ID плейлистов, уже содержащих этот трек
 * @param onDismiss колбэк закрытия диалога
 * @param onPlaylistSelected колбэк выбора плейлиста (добавление)
 * @param onPlaylistRemoved колбэк удаления трека из плейлиста
 * @param onCreateNewPlaylist колбэк создания нового плейлиста
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun AddToPlaylistDialog(
    playlists: List<PlaylistWithTrackCount>,
    trackId: Long,
    playlistsContainingTrack: Set<Long>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (PlaylistWithTrackCount) -> Unit,
    onPlaylistRemoved: (PlaylistWithTrackCount) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.dialog_add_to_playlist),
        dismissText = stringResource(R.string.btn_cancel),
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Кнопка создания нового плейлиста
            CreateNewPlaylistItem(
                onClick = onCreateNewPlaylist
            )

            if (playlists.isEmpty()) {
                // Пустое состояние
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_no_playlists),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AudioPlayerThemeExtended.colors.subtleText
                    )
                }
            } else {
                // Список плейлистов
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playlists) { playlist ->
                        val isTrackInPlaylist = playlistsContainingTrack.contains(playlist.playlistId)

                        ModernSelectionItem(
                            title = playlist.name,
                            subtitle = if (isTrackInPlaylist) {
                                stringResource(R.string.click_to_remove)
                            } else {
                                null
                            },
                            selected = isTrackInPlaylist,
                            onClick = {
                                if (isTrackInPlaylist) {
                                    onPlaylistRemoved(playlist)
                                } else {
                                    onPlaylistSelected(playlist)
                                }
                                onDismiss()
                            },
                            icon = {
                                PlaylistIcon(selected = isTrackInPlaylist)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Элемент "Создать новый плейлист"
 */
@Composable
private fun CreateNewPlaylistItem(
    onClick: () -> Unit
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(extendedColors.cardBackgroundElevated)
            .clickable(
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                interactionSource = null,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Иконка
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(extendedColors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        // Текст
        Text(
            text = stringResource(R.string.create_new_playlist),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Иконка плейлиста
 */
@Composable
private fun PlaylistIcon(
    selected: Boolean
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) extendedColors.accentSoft
                else extendedColors.cardBorder.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else extendedColors.subtleText,
            modifier = Modifier.size(22.dp)
        )
    }
}
