package com.example.soccerworld.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary brand — Midnight Navy ──────────────────────────────────────────
val BrandNavy       = Color(0xFF0D1B2A)   // deepest background
val BrandNavyMid    = Color(0xFF1A2F45)   // surface / card
val BrandNavyLight  = Color(0xFF243B55)   // elevated surface

// ── Accent — Vivid Emerald Cyan ────────────────────────────────────────────
val AccentEmerald   = Color(0xFF00D9A3)   // primary CTA / active
val AccentEmeraldDim= Color(0xFF00A87E)   // darker accent

// ── Legacy Sofascore blue (kept for gradients & highlights) ────────────────
val SofascoreBlue   = Color(0xFF374DF5)
val SofascoreBlueDark= Color(0xFF2A3BC7)

// ── Text ───────────────────────────────────────────────────────────────────
val TextDark        = Color(0xFF0D1B2A)
val TextOnDark      = Color(0xFFEFF4FF)
val TextSecondary   = Color(0xFF8FA3B8)
val DividerColor    = Color(0xFFE2EAF0)

// ── Semantic ───────────────────────────────────────────────────────────────
val LiveRed         = Color(0xFFFF3B5C)
val WinnerText      = Color(0xFF0D1B2A)
val LoserText       = Color(0xFF8FA3B8)

// ── Legacy (kept for older screens) ───────────────────────────────────────
val PrimaryColor    = Color(0xFF6D28D9)
val AccentColor     = Color(0xFF22D3EE)
val BackgroundColor = Color(0xFFF4F6FB)
val SurfaceColor    = Color(0xFFFFFFFF)
val TextPrimary     = Color(0xFF111827)

data class SoccerColors(
    val liveRed: Color = LiveRed,
    val winnerText: Color = WinnerText,
    val loserText: Color = LoserText,
    val zoneChampionsLeague: Color = Color(0xFF00D9A3),  // Emerald
    val zoneEuropaLeague: Color    = Color(0xFFF59E0B),  // Amber
    val zoneConferenceLeague: Color= Color(0xFF60A5FA),  // Sky blue
    val zoneRelegation: Color      = Color(0xFFFF3B5C),  // Vivid red
    val homeColor: Color = Color(0xFF00D9A3),
    val awayColor: Color = Color(0xFF374DF5)
)

val LocalSoccerColors = androidx.compose.runtime.compositionLocalOf { SoccerColors() }
