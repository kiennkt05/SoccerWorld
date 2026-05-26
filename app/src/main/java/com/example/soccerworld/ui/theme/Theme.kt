package com.example.soccerworld.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.SideEffect

// ── Dark scheme: Midnight Navy + Emerald accent ────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = AccentEmerald,
    onPrimary            = BrandNavy,
    primaryContainer     = AccentEmeraldDim,
    onPrimaryContainer   = TextOnDark,
    secondary            = SofascoreBlue,
    onSecondary          = Color.White,
    secondaryContainer   = BrandNavyLight,
    onSecondaryContainer = TextOnDark,
    background           = BrandNavy,
    onBackground         = TextOnDark,
    surface              = BrandNavyMid,
    onSurface            = TextOnDark,
    surfaceVariant       = BrandNavyLight,
    onSurfaceVariant     = TextSecondary,
    outline              = Color(0xFF2E4560),
    outlineVariant       = Color(0xFF1E3550),
    error                = LiveRed,
    onError              = Color.White
)

// ── Light scheme: Clean white + Midnight navy + Emerald ───────────────────
private val LightColorScheme = lightColorScheme(
    primary              = BrandNavy,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFDFF9F2),   // soft emerald tint
    onPrimaryContainer   = BrandNavy,
    secondary            = AccentEmerald,
    onSecondary          = BrandNavy,
    secondaryContainer   = Color(0xFFE8FDF7),
    onSecondaryContainer = BrandNavy,
    background           = Color(0xFFF6F9FC),
    onBackground         = TextDark,
    surface              = Color.White,
    onSurface            = TextDark,
    surfaceVariant       = Color(0xFFF0F4F8),
    onSurfaceVariant     = TextSecondary,
    outline              = DividerColor,
    outlineVariant       = Color(0xFFE0EAF3),
    error                = LiveRed,
    onError              = Color.White
)

@Composable
fun SoccerWorldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSoccerColors provides SoccerColors()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
