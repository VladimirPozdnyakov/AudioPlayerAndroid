package com.foxelectronic.audioplayer.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.network.GitHubRelease
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Диалог для отображения информации о доступном обновлении
 *
 * @param release Информация о релизе из GitHub API
 * @param onDownload Вызывается при нажатии на кнопку "Скачать" с URL релиза
 * @param onDismiss Вызывается при закрытии диалога
 */
@Composable
fun UpdateDialog(
    release: GitHubRelease,
    onDownload: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.update_dialog_title),
        confirmText = stringResource(R.string.btn_download),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            onDownload(release.htmlUrl)
            onDismiss()
        },
        onDismiss = onDismiss,
        isDestructive = false
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Иконка обновления
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Номер версии
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.update_dialog_version_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.subtleText
                    )
                    Text(
                        text = release.tagName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Описание
            Text(
                text = stringResource(R.string.update_dialog_description),
                style = MaterialTheme.typography.bodyMedium,
                color = extendedColors.subtleText,
                textAlign = TextAlign.Center
            )
        }
    }
}
