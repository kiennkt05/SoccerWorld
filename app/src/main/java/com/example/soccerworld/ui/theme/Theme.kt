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

// ── Dark scheme: Deep navy surfaces, Sofascore Blue accents ────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF7B8CFF),   // lighter blue for dark bg
    onPrimary            = Color(0xFF001A9E),
    primaryContainer     = SofascoreBlueDark,
    onPrimaryContainer   = Color(0xFFDEE0FF),
    secondary            = Color(0xFFCBBEFF),
    onSecondary          = Color(0xFF332B6A),
    secondaryContainer   = DarkSurfaceVar,
    onSecondaryContainer = TextOnDark,
    background           = DarkBackground,
    onBackground         = TextOnDark,
    surface              = DarkSurface,
    onSurface            = TextOnDark,
    surfaceVariant       = DarkSurfaceVar,
    onSurfaceVariant     = TextSecondary,
    outline              = Color(0xFF2C2C40),
    outlineVariant       = Color(0xFF222236),
    error                = LiveRed,
    onError              = Color.White
)

// ── Light scheme: Sofascore Blue primary, clean white surfaces ─────────────
private val LightColorScheme = lightColorScheme(
    primary              = SofascoreBlue,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFE8EBFF),   // soft blue tint
    onPrimaryContainer   = SofascoreBlueDark,
    secondary            = SofascorePurple,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFF0ECFF),
    onSecondaryContainer = SofascorePurple,
    background           = LightBackground,
    onBackground         = TextDark,
    surface              = LightSurface,
    onSurface            = TextDark,
    surfaceVariant       = LightSurfaceVar,
    onSurfaceVariant     = TextSecondary,
    outline              = DividerColor,
    outlineVariant       = Color(0xFFE0E0EC),
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
            window.statusBarColor = SofascoreBlueDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
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
