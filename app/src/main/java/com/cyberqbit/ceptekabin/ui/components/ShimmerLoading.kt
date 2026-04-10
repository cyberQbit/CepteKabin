package com.cyberqbit.ceptekabin.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.cyberqbit.ceptekabin.ui.theme.*

/**
 * Premium shimmer effect for loading states
 * iOS-style: subtle, elegant, not distracting
 */
@Composable
fun shimmerBrush(isDark: Boolean): Brush {
    val highlightColor = if (isDark) {
        Grey700.copy(alpha = 0.4f)
    } else {
        Grey200.copy(alpha = 0.8f)
    }
    val baseColor = if (isDark) {
        Grey800.copy(alpha = 0.6f)
    } else {
        Grey100.copy(alpha = 0.6f)
    }
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )
}

/**
 * Shimmer loading card - mimics GlassCard while loading
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(shimmerBrush(isDark))
    ) {
        content()
    }
}

/**
 * Full shimmer loading card with predefined structure
 */
@Composable
fun ShimmerLoadingCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    ShimmerCard(modifier = modifier, isDark = isDark) {
        Column(Modifier.padding(16.dp)) {
            // Title placeholder
            Box(Modifier.fillMaxWidth(0.4f).height(16.dp).clip(RoundedCornerShape(8.dp)).shimmerModifier(isDark))
            Spacer(Modifier.height(12.dp))
            // Subtitle placeholder  
            Box(Modifier.fillMaxWidth(0.7f).height(12.dp).clip(RoundedCornerShape(6.dp)).shimmerModifier(isDark))
            Spacer(Modifier.height(8.dp))
            // Content lines
            Box(Modifier.fillMaxWidth(0.9f).height(12.dp).clip(RoundedCornerShape(6.dp)).shimmerModifier(isDark))
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth(0.6f).height(12.dp).clip(RoundedCornerShape(6.dp)).shimmerModifier(isDark))
        }
    }
}

private fun Modifier.shimmerModifier(isDark: Boolean): Modifier = composed {
    this.background(shimmerBrush(isDark))
}

/**
 * Shimmer for list items (like kiyafet cards)
 */
@Composable
fun ShimmerListItem(
    isDark: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        // Image placeholder
        Box(
            Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerBrush(isDark))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Box(Modifier.fillMaxWidth(0.5f).height(14.dp).clip(RoundedCornerShape(7.dp)).background(shimmerBrush(isDark)))
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth(0.35f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush(isDark)))
        }
    }
}

/**
 * Full screen shimmer loading state
 */
@Composable
fun ShimmerLoadingScreen(
    isDark: Boolean = true,
    itemCount: Int = 5
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        repeat(itemCount) {
            ShimmerListItem(isDark = isDark)
            if (it < itemCount - 1) {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
