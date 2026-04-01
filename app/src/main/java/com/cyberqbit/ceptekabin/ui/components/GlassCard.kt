package com.cyberqbit.ceptekabin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cyberqbit.ceptekabin.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Dark: subtle teal-tinted glass; Light: warm cream glass
    val baseColor = if (isDark) GlassDark else GlassLight
    val highlightColor = if (isDark) GlassDarkHighlight else GlassLightHighlight
    val borderColor = if (isDark) GlassDarkBorder else GlassLightBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        highlightColor.copy(alpha = 0.35f),
                        baseColor,
                        baseColor.copy(alpha = 0.55f)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
    val isDark = isSystemInDarkTheme()

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
    val isDark = isSystemInDarkTheme()

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
    val isDark = isSystemInDarkTheme()
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
