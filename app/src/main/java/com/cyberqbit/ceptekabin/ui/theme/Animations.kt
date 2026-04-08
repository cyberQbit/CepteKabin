package com.cyberqbit.ceptekabin.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * iOS-style spring animations for natural feel
 */
private val StandardSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

private val GentleSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow
)

/**
 * Press scale animation - iOS style
 * Element scales down slightly when pressed, springs back on release
 */
fun Modifier.iOSPressEffect(
    enabled: Boolean = true,
    scaleDown: Float = 0.96f,
    onClick: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    this
        .scale(if (isPressed && enabled) scaleDown else 1f)
        .clickable(
            interactionSource = interactionSource,
            indication = null,  // We handle visual feedback ourselves
            onClick = onClick
        )
}

/**
 * Gentle scale on press for cards
 */
fun Modifier.cardPressEffect(
    onClick: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "card_scale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Smooth fade + scale enter/exit animation spec
 */
fun AnimatedVisibilitySpec(
    initialScale: Float = 0.92f,
    targetScale: Float = 1f
): ContentTransform {
    return fadeIn(
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + scaleIn(
        initialScale = initialScale,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    ) togetherWith fadeOut(
        animationSpec = tween(200, easing = FastOutLinearInEasing)
    ) + scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(200, easing = FastOutLinearInEasing)
    )
}

/**
 * Content appears with subtle slide up + fade
 */
@Composable
fun SlideUpFadeIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
            targetOffsetY = { it / 4 },
            animationSpec = tween(200)
        )
    ) {
        content()
    }
}

/**
 * Shimmer animation spec for loading states
 */
val ShimmerDuration = 1200
val ShimmerEasing = FastOutSlowInEasing

/**
 * Page transition - iOS style horizontal slide
 */
val PageTransitionIn = slideInHorizontally(
    initialOffsetX = { it / 3 },
    animationSpec = spring(dampingRatio = 0.85f, stiffness = 250f)
) + fadeIn(animationSpec = tween(300))

val PageTransitionOut = slideOutHorizontally(
    targetOffsetX = { -it / 3 },
    animationSpec = tween(250)
) + fadeOut(animationSpec = tween(250))
