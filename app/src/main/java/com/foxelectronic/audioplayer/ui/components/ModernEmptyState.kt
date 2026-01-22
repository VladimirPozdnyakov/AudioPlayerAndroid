package com.foxelectronic.audioplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Modern empty state component with card-based design
 *
 * Displays an icon in a rounded container, a title, and optional subtitle
 * centered within a card container. Features fade-in animation on appearance.
 *
 * @param icon The icon to display in the centered container
 * @param title Main title text describing the empty state
 * @param subtitle Optional secondary text with additional context
 * @param modifier Modifier to be applied to the root component
 */
@Composable
fun ModernEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val dimens = AudioPlayerThemeExtended.dimens

    // Fade-in animation on appearance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "emptyStateAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(RoundedCornerShape(dimens.cardCornerRadius))
            .background(extendedColors.cardBackground)
            .border(
                width = 1.dp,
                color = extendedColors.cardBorder,
                shape = RoundedCornerShape(dimens.cardCornerRadius)
            )
            .padding(dimens.sectionSpacing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
        ) {
            // Icon container with accent soft background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(extendedColors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Title text
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Optional subtitle
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.subtleText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
