package com.foxelectronic.audioplayer.ui.settings.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.settings.utils.FolderUtils
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Секция управления папками с современным карточным дизайном
 */
@Composable
fun FoldersSection(
    folders: List<String>,
    onAddFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AudioPlayerThemeExtended.dimens

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
    ) {
        // Список папок
        folders.forEach { folder ->
            val displayName = remember(folder) { FolderUtils.getFolderDisplayName(folder) }
            val isDefault = FolderUtils.isDefaultFolder(folder)

            FolderItem(
                displayName = displayName,
                isDefault = isDefault,
                onRemove = if (!isDefault) {{ onRemoveFolder(folder) }} else null
            )
        }

        Spacer(Modifier.height(8.dp))

        // Кнопка добавления папки
        AddFolderButton(onClick = onAddFolder)
    }
}

@Composable
private fun FolderItem(
    displayName: String,
    isDefault: Boolean,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val dimens = AudioPlayerThemeExtended.dimens
    val extendedColors = AudioPlayerThemeExtended.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
            .background(extendedColors.cardBackground)
            .border(
                width = 1.dp,
                color = extendedColors.cardBorder,
                shape = RoundedCornerShape(dimens.cardCornerRadiusSmall)
            )
            .padding(dimens.cardPaddingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Иконка папки
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(extendedColors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDefault) Icons.Rounded.FolderSpecial else Icons.Rounded.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        // Название и метка
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isDefault) {
                Text(
                    text = stringResource(R.string.default_music_folder),
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.subtleText
                )
            }
        }

        // Метка "По умолчанию" или кнопка удаления
        if (isDefault) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(extendedColors.accentSoft)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_default),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (onRemove != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.delete_folder_desc, displayName),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddFolderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AudioPlayerThemeExtended.dimens
    val extendedColors = AudioPlayerThemeExtended.colors

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "addButtonScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
            .background(extendedColors.accentSoft)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(dimens.cardCornerRadiusSmall)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
            .padding(dimens.cardPaddingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.add_folder),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
