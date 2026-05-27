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

// ── Dark scheme: Premium Deep Forest & Neon Mint Accents ──────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF66BB6A),   // Soft green-mint for high legibility on dark
    onPrimary            = Color(0xFF072117),   // Deep forest green contrast
    primaryContainer     = BrandGreenDark,
    onPrimaryContainer   = Color(0xFFE8F5E9),
    secondary            = Color(0xFF81C784),   // Soft sage green
    onSecondary          = Color(0xFF072117),
    secondaryContainer   = DarkSurfaceVar,
    onSecondaryContainer = TextOnDark,
    background           = DarkBackground,
    onBackground         = TextOnDark,
    surface              = DarkSurface,
    onSurface            = TextOnDark,
    surfaceVariant       = DarkSurfaceVar,
    onSurfaceVariant     = TextSecondary,
    outline              = Color(0xFF1C2C24),   // Soft slate green border
    outlineVariant       = Color(0xFF121D18),
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
            window.statusBarColor = BrandGreenDark.toArgb() // Beautiful deep forest status bar
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
