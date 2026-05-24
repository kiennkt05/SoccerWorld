package com.example.soccerworld.ui.theme

import androidx.compose.ui.graphics.Color

// Sofascore-style colors
val SofascoreBlue = Color(0xFF374DF5)
val SofascoreBlueDark = Color(0xFF2A3BC7)

val TextDark = Color(0xFF1A1A2E)
val TextSecondary = Color(0xFF6C6C80)
val DividerColor = Color(0xFFE8E8E8)
val LiveRed = Color(0xFFE74C3C)
val WinnerText = Color(0xFF1A1A2E)
val LoserText = Color(0xFF999999)

// Legacy (kept for other screens)
val PrimaryColor = Color(0xFF6D28D9)
val AccentColor = Color(0xFF22D3EE)
val BackgroundColor = Color(0xFFF4F6FB)
val SurfaceColor = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF111827)

data class SoccerColors(
    val liveRed: Color = LiveRed,
    val winnerText: Color = WinnerText,
    val loserText: Color = LoserText,
    val zoneChampionsLeague: Color = Color(0xFF1E3A8A), // Dark Blue
    val zoneEuropaLeague: Color = Color(0xFFD97706), // Amber
    val zoneConferenceLeague: Color = Color(0xFF15803D), // Green
    val zoneRelegation: Color = Color(0xFFDC2626), // Red
    val homeColor: Color = Color(0xFF00B050),
    val awayColor: Color = Color(0xFF2B44FF)
)

val LocalSoccerColors = androidx.compose.runtime.compositionLocalOf { SoccerColors() }
