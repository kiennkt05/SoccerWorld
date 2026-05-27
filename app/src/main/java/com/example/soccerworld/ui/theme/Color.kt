package com.example.soccerworld.ui.theme

import androidx.compose.ui.graphics.Color

// ── Sofascore Brand Colors ─────────────────────────────────────────────────
val SofascoreBlue     = Color(0xFF374DF5)   // Primary, CTAs, active tab
val SofascorePurple   = Color(0xFF5B3FC4)   // Gradient mid-point
val SofascoreBlueDark = Color(0xFF2A3BC7)   // Pressed states, status bar
val DeepPurple        = Color(0xFF7B2FA0)   // Gradient end (Profile header)

// ── Legacy Aliases (mapped to Sofascore for backward compatibility) ────────
val BrandNavy         = SofascoreBlue
val BrandNavyMid      = SofascoreBlueDark
val BrandNavyLight    = SofascorePurple
val AccentEmerald     = SofascoreBlue
val AccentEmeraldDim  = SofascoreBlueDark

// ── Text & Background ──────────────────────────────────────────────────────
val TextDark        = Color(0xFF1A1A2E)   // Primary text (light mode)
val TextOnDark      = Color(0xFFE8E8F0)   // Primary text (dark mode)
val TextSecondary   = Color(0xFF6B7B8D)   // Secondary text (both modes)
val DividerColor    = Color(0xFFE8E8EF)   // Borders, dividers

val LightBackground = Color(0xFFF5F5F9)
val LightSurface    = Color(0xFFFFFFFF)
val LightSurfaceVar = Color(0xFFF0F2F7)
val DarkBackground  = Color(0xFF12121F)
val DarkSurface     = Color(0xFF1E1E32)
val DarkSurfaceVar  = Color(0xFF2A2A42)

// ── Semantic — Sport-specific ──────────────────────────────────────────────
val LiveRed         = Color(0xFFFF4654)
val WinGreen        = Color(0xFF1DB954)
val DrawAmber       = Color(0xFFF5A623)
val FavoriteGold    = Color(0xFFFFC107)
val WinnerText      = TextDark
val LoserText       = TextSecondary

// ── Legacy (kept for older screens) ───────────────────────────────────────
val PrimaryColor    = SofascoreBlue
val AccentColor     = SofascorePurple
val BackgroundColor = LightBackground
val SurfaceColor    = LightSurface
val TextPrimary     = TextDark

data class SoccerColors(
    val liveRed: Color = LiveRed,
    val winnerText: Color = WinnerText,
    val loserText: Color = LoserText,
    val zoneChampionsLeague: Color = Color(0xFF025492),
    val zoneEuropaLeague: Color    = Color(0xFFF5A623),
    val zoneConferenceLeague: Color= Color(0xFF1DB954),
    val zoneRelegation: Color      = Color(0xFFFF4654),
    val homeColor: Color = SofascoreBlue,
    val awayColor: Color = SofascorePurple
)

val LocalSoccerColors = androidx.compose.runtime.compositionLocalOf { SoccerColors() }
