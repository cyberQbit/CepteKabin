package com.cyberqbit.ceptekabin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = TextPrimaryDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = TextPrimaryDark,
    tertiary = AccentGold,
    onTertiary = BackgroundDark,
    tertiaryContainer = AccentGold.copy(alpha = 0.15f),
    onTertiaryContainer = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Grey600.copy(alpha = 0.4f),
    outlineVariant = Grey700.copy(alpha = 0.3f),
    error = Error,
    onError = White,
    errorContainer = Error.copy(alpha = 0.15f),
    onErrorContainer = ErrorLight,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryLight.copy(alpha = 0.1f),
    onPrimaryContainer = PrimaryDark,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryLight.copy(alpha = 0.08f),
    onSecondaryContainer = SecondaryDark,
    tertiary = AccentGold,
    onTertiary = TextPrimaryLight,
    tertiaryContainer = AccentGold.copy(alpha = 0.08f),
    onTertiaryContainer = TextPrimaryLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Grey300,
    outlineVariant = Grey200,
    error = Error,
    onError = White,
    errorContainer = Error.copy(alpha = 0.08f),
    onErrorContainer = Error,
)

@Composable
fun CepteKabinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: status bar transparent
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
