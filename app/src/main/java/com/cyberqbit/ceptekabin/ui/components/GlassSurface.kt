package com.cyberqbit.ceptekabin.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cyberqbit.ceptekabin.ui.theme.*

/**
 * GlassSurface - iOS Style Compact Container
 * Used inside cards for icons, small containers
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    cornerRadius: Int = 12,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    val bgColor = if (isDark) {
        Grey800.copy(alpha = 0.5f)
    } else {
        Grey100.copy(alpha = 0.7f)
    }
    val borderColor = if (isDark) {
        Grey700.copy(alpha = 0.3f)
    } else {
        Grey200.copy(alpha = 0.4f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(0.5.dp, borderColor, shape),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * GlassIconContainer - specifically for icon backgrounds
 */
@Composable
fun GlassIconContainer(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isDark: Boolean = isSystemInDarkTheme(),
    iconTint: Color = PrimaryLight,
    icon: @Composable () -> Unit
) {
    GlassSurface(
        modifier = modifier.size(size),
        isDark = isDark,
        cornerRadius = 14,
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
