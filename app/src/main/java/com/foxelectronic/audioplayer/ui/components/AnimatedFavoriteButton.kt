package com.foxelectronic.audioplayer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended
import kotlinx.coroutines.delay

@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    favoriteColor: Color = MaterialTheme.colorScheme.primary,
    unfavoriteColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    var scale by remember { mutableStateOf(1f) }
    val targetScale = 1f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "favoritePressScale"
    )

    val currentScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "favoriteScaleAnimation"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isFavorite) extendedColors.accentSoft
                else Color.Transparent
            )
            .border(
                if (isFavorite) 1.5.dp else 1.dp,
                if (isFavorite) favoriteColor.copy(alpha = 0.5f)
                else extendedColors.cardBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = favoriteColor),
                onClick = {
                    onToggleFavorite()
                    scale = 1.2f
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentDesc = if (isFavorite)
            stringResource(R.string.menu_remove_from_favorites)
        else
            stringResource(R.string.menu_add_to_favorites)
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = contentDesc,
            tint = if (isFavorite) favoriteColor else extendedColors.iconTint,
            modifier = Modifier.size(iconSize).scale(currentScale)
        )
    }

    LaunchedEffect(scale) {
        if (scale != targetScale) {
            delay(200)
            scale = targetScale
        }
    }
}
