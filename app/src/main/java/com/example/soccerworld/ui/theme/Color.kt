package com.example.soccerworld.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * SOCCERWORLD NEW HARMONIOUS DESIGN SYSTEM (60-30-10 RULE)
 *
 * 60% Dominant (Background & Canvas):
 *   - Light Mode: Soft, clean light green-gray tint (#F4F6F5) & Pure White Surfaces
 *   - Dark Mode: Premium deep charcoal-teal (#0B100E) & Dark Slate-Green Surfaces
 *
 * 30% Secondary (Structure & Headers):
 *   - Deep Forest Green (#0F3E2D) & Vibrant Sports Teal (#1B5E46)
 *   - Gives the app a high-end, premium "pitch/grass" athletic vibe.
 *
 * 10% Accent (Call-to-Action & Focus):
 *   - Vibrant Neon Coral/Orange (#FF6D00) & Bright Neon Mint (#00FF88)
 *   - Draws focus instantly to live scores, CTA buttons, and active tabs.
 */

// ── 30% Secondary: Brand Green Tones ────────────────────────────────────────
val BrandGreenDark   = Color(0xFF0F3E2D)   // Premium Deep Forest (60% dark headers)
val BrandGreenMedium = Color(0xFF1B5E46)   // Sports Teal (Primary buttons & core details)
val BrandGreenLight  = Color(0xFF2E8B69)   // Medium Emerald (Subtle borders / active indicators)

// ── 10% Accent: Neon Focus Tones ────────────────────────────────────────────
val AccentNeonMint   = Color(0xFF00E676)   // Bright Neon Mint (Live badges, success status)
val AccentNeonOrange = Color(0xFFFF6D00)   // High-Contrast Neon Coral/Orange (CTAs, favorites)

// ── Backwards Compatibility Mapping (Zero code breakage across existing screens) ──
val SofascoreBlue     = BrandGreenMedium   // Primary color for tabs & buttons
val SofascorePurple   = BrandGreenLight    // Secondary gradient mid-point
val SofascoreBlueDark = BrandGreenDark     // Deep header / pressed states
val DeepPurple        = AccentNeonOrange   // Profile orange avatar & highlight accent

val BrandNavy         = BrandGreenDark
val BrandNavyMid      = Color(0xFF072117)  // Super deep green
val BrandNavyLight    = BrandGreenMedium
val AccentEmerald     = AccentNeonMint
val AccentEmeraldDim  = Color(0xFF00B248)

// ── 60% Dominant: Text & Background Tones (High WCAG Contrast) ────────────────
val TextDark        = Color(0xFF111815)   // Deep black-green for light mode (Contrast > 10:1)
val TextOnDark      = Color(0xFFECEFF1)   // Soft off-white for dark mode (Contrast > 8:1)
val TextSecondary   = Color(0xFF556860)   // Slate gray-green (Legible subtexts, > 4.5:1)
val DividerColor    = Color(0xFFE2EBE7)   // Clean soft borders

val LightBackground = Color(0xFFF4F6F5)   // 60% Light background (Ultra soft, eyes-friendly)
val LightSurface    = Color(0xFFFFFFFF)   // Pure White for Cards (30%)
val LightSurfaceVar = Color(0xFFEAEFEA)   // Soft light gray-green

val DarkBackground  = Color(0xFF0B100E)   // 60% Dark background
val DarkSurface     = Color(0xFF121B17)   // Deep Pine Surface (30%)
val DarkSurfaceVar  = Color(0xFF1A2621)   // Soft dark green-gray

// ── Semantic Colors (Accessible, Globally Standardized) ────────────────────────
val LiveRed         = Color(0xFFFF334B)   // Neon Live Red (Error & Live match tag)
val WinGreen        = AccentNeonMint      // Success Green
val DrawAmber       = Color(0xFFFFB300)   // Warning Amber
val FavoriteGold    = Color(0xFFFFC107)   // Gold Star for favorites
val WinnerText      = TextDark
val LoserText       = TextSecondary

data class SoccerColors(
    val liveRed: Color = LiveRed,
    val winnerText: Color = WinnerText,
    val loserText: Color = LoserText,
    val zoneChampionsLeague: Color = Color(0xFF0D47A1), // Royal Blue for UCL
    val zoneEuropaLeague: Color    = Color(0xFFE65100), // Orange for UEL
    val zoneConferenceLeague: Color= Color(0xFF2E7D32), // Green for UECL
    val zoneRelegation: Color      = Color(0xFFB71C1C), // Deep Red for Relegation
    val homeColor: Color = BrandGreenMedium,
    val awayColor: Color = AccentNeonOrange
)

val LocalSoccerColors = androidx.compose.runtime.compositionLocalOf { SoccerColors() }
