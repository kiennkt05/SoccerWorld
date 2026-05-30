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
    background           = Color(0xFF0F131A),   // Deep Slate Blue background
    onBackground         = Color(0xFFF1F5F9),   // Ice white text
    surface              = Color(0xFF151B26),   // Slightly elevated card surface
    onSurface            = Color(0xFFF1F5F9),   // Ice white text
    surfaceVariant       = Color(0xFF1E293B),
    onSurfaceVariant     = Color(0xFF94A3B8),
    outline              = Color(0xFF232C3F),   // Division outlines
    outlineVariant       = Color(0xFF1A2230),
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
            window.statusBarColor = if (darkTheme) Color(0xFF0F131A).toArgb() else BrandGreenDark.toArgb()
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

