package com.cyberqbit.ceptekabin.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cyberqbit.ceptekabin.ui.theme.*

/**
 * Premium GlassCard - iOS Style
 * - Subtle blur effect (not too heavy)
 * - Consistent 16.dp corner radius
 * - Soft shadow for depth
 * - Smooth press feedback
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    cornerRadius: Int = 16,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius.dp)

    val bgColor = if (isDark) {
        SurfaceDark.copy(alpha = 0.7f)
    } else {
        White.copy(alpha = 0.85f)
    }

    val borderColor = if (isDark) {
        Grey700.copy(alpha = 0.3f)
    } else {
        Grey200.copy(alpha = 0.5f)
    }

    val contentModifier = modifier
        .clip(shape)
        .background(bgColor)
        .border(1.dp, borderColor, shape)

    if (onClick != null) {
        Column(
            modifier = contentModifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            content = content
        )
    } else {
        Column(
            modifier = contentModifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassCardWithHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            actions()
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = true

    val backgroundColor = if (isDark) GlassDarkSurface else GlassLightSurface
    val highlightColor = if (isDark) GlassDarkHighlight else GlassLightHighlight
    val borderColor = if (isDark) GlassDarkBorder.copy(alpha = 0.25f) else GlassLightBorder.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        highlightColor.copy(alpha = 0.18f),
                        backgroundColor
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = true

    val bgColor = if (enabled) {
        if (isDark) PrimaryDark else PrimaryLight
    } else {
        Grey500
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (enabled) White else Grey300,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Grey400
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        content = content
    )
}

@Composable
fun GlassOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = true
    val borderColor = if (enabled) {
        if (isDark) GlassDarkBorder else GlassLightBorder
    } else {
        Grey500
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = if (isDark) TextPrimaryDark else TextPrimaryLight
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        content = content
    )
}
