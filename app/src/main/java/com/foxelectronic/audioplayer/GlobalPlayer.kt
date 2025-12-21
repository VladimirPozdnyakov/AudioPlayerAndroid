package com.foxelectronic.audioplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.foxelectronic.audioplayer.ui.components.AnimatedPlayPauseButton
import com.foxelectronic.audioplayer.ui.components.AnimatedSkipButton
import com.foxelectronic.audioplayer.ui.components.RepeatModeButton
import com.foxelectronic.audioplayer.ui.components.ShuffleModeButton
import com.foxelectronic.audioplayer.ui.components.SkipDirection
import com.foxelectronic.audioplayer.ui.components.toTimeString

@Composable
fun GlobalPlayerBar(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    onMiniPlayerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) {
        val currentTrack = uiState.tracks.getOrNull(uiState.currentIndex)
        val progress = if (uiState.durationMs > 0) {
            (uiState.positionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                .clickable { onMiniPlayerClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Информация о треке
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    currentTrack?.let { track ->
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = track.artist ?: "Неизвестный исполнитель",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${uiState.positionMs.toTimeString()} / ${uiState.durationMs.toTimeString()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Элементы управления
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShuffleModeButton(
                        isEnabled = uiState.isShuffleModeEnabled,
                        onToggle = { viewModel.toggleShuffleMode() },
                        enabled = uiState.repeatMode != Player.REPEAT_MODE_ONE
                    )

                    AnimatedSkipButton(
                        direction = SkipDirection.Previous,
                        onClick = { viewModel.previous() }
                    )

                    AnimatedPlayPauseButton(
                        isPlaying = uiState.isPlaying,
                        onToggle = { if (uiState.isPlaying) viewModel.pause() else viewModel.resume() },
                        size = 40.dp,
                        iconSize = 20.dp
                    )

                    AnimatedSkipButton(
                        direction = SkipDirection.Next,
                        onClick = { viewModel.next() }
                    )

                    RepeatModeButton(
                        repeatMode = uiState.repeatMode,
                        onToggle = { viewModel.toggleRepeatMode() }
                    )
                }
            }

            // Полоса прогресса внизу
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        }
    }
}
