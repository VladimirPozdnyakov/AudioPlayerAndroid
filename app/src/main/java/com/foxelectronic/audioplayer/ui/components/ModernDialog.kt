package com.foxelectronic.audioplayer.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Современный диалог с карточным дизайном
 */
@Composable
fun ModernDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    confirmText: String? = null,
    dismissText: String? = null,
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    isDestructive: Boolean = false,
    content: @Composable () -> Unit
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = extendedColors.cardBackground,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Заголовок
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Подзаголовок (опционально)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = extendedColors.subtleText
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Контент
                content()

                // Кнопки
                if (confirmText != null || dismissText != null) {
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismissText != null && onDismiss != null) {
                            ModernDialogButton(
                                text = dismissText,
                                onClick = onDismiss,
                                isPrimary = false
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        if (confirmText != null && onConfirm != null) {
                            ModernDialogButton(
                                text = confirmText,
                                onClick = onConfirm,
                                isPrimary = true,
                                enabled = confirmEnabled,
                                isDestructive = isDestructive
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Кнопка для диалога
 */
@Composable
fun ModernDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    enabled: Boolean = true,
    isDestructive: Boolean = false
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )

    val backgroundColor = when {
        !enabled -> extendedColors.cardBorder
        isDestructive && isPrimary -> MaterialTheme.colorScheme.errorContainer
        isPrimary -> extendedColors.accentSoft
        else -> Color.Transparent
    }

    val textColor = when {
        !enabled -> extendedColors.subtleText
        isDestructive && isPrimary -> MaterialTheme.colorScheme.error
        isPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        !enabled -> Color.Transparent
        isPrimary -> Color.Transparent
        else -> extendedColors.cardBorder
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (!isPrimary && enabled) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))
                } else Modifier
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = rememberRipple(color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * Современный элемент выбора с карточным дизайном
 */
@Composable
fun ModernSelectionItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "itemScale"
    )

    val backgroundColor = if (selected) {
        extendedColors.accentSoft
    } else {
        extendedColors.cardBackgroundElevated
    }

    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        extendedColors.cardBorder
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Иконка (опционально)
        if (icon != null) {
            icon()
        }

        // Контент
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.subtleText
                )
            }
        }

        // Индикатор выбора
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else extendedColors.cardBorder
                )
                .then(
                    if (!selected) {
                        Modifier.border(2.dp, extendedColors.cardBorder, RoundedCornerShape(11.dp))
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}
