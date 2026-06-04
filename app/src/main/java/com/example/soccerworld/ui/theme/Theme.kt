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
import androidx.compose.runtime.mutableStateOf

// ── Dark scheme: Ocean Slate (Option B) ──────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF38BDF8),   // Ocean Blue
    onPrimary            = Color(0xFF0F131A),   // Deep slate contrast
    primaryContainer     = Color(0xFF1E293B),
    onPrimaryContainer   = Color(0xFFE2E8F0),
    secondary            = Color(0xFF0EA5E9),
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF151B26),
    onSecondaryContainer = Color(0xFFF1F5F9),
    tertiary             = AccentNeonMint,
    onTertiary           = Color(0xFF003300),
    tertiaryContainer    = Color(0xFF004D26),
    onTertiaryContainer  = Color(0xFFA5F3C4),
    background           = Color(0xFF2B3A4F),   // Deep Slate Blue background (Much Lighter)
    onBackground         = Color(0xFFF1F5F9),   // Ice white text
    surface              = Color(0xFF374862),   // Elevated card surface (Much Lighter)
    onSurface            = Color(0xFFF1F5F9),   // Ice white text
    surfaceVariant       = Color(0xFF475975),
    onSurfaceVariant     = Color(0xFFCBD5E1),
    outline              = Color(0xFF64748B),   // Division outlines
    outlineVariant       = Color(0xFF475569),
    error                = LiveRed,
    onError              = Color.White
)

// ── Light scheme: Premium Sports Teal & Clean Light Background ──────────────
private val LightColorScheme = lightColorScheme(
    primary              = BrandGreenMedium,   // Premium Teal
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFE2F0EC),   // Soft minty-green container tint
    onPrimaryContainer   = BrandGreenDark,
    secondary            = BrandGreenLight,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFEBF5F0),   // Light emerald-green tint
    onSecondaryContainer = BrandGreenMedium,
    tertiary             = Color(0xFF00C853),   // Slightly darker emerald for text readability
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFE0F7E9),
    onTertiaryContainer  = Color(0xFF003300),
    background           = LightBackground,
    onBackground         = TextDark,
    surface              = LightSurface,
    onSurface            = TextDark,
    surfaceVariant       = LightSurfaceVar,
    onSurfaceVariant     = TextSecondary,
    outline              = DividerColor,
    outlineVariant       = Color(0xFFD4E2DC),   // Soft slate-green borders
    error                = LiveRed,
    onError              = Color.White
)

object ThemeConfig {
    val appThemeState = mutableStateOf("system")
}

@Composable
fun SoccerWorldTheme(
    darkTheme: Boolean = when (ThemeConfig.appThemeState.value) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    },
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

    val soccerColors = if (darkTheme) DarkSoccerColors else LightSoccerColors

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSoccerColors provides soccerColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
