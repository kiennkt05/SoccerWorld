package com.example.soccerworld.ui.theme

import androidx.compose.runtime.compositionLocalOf
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

// ═══════════════════════════════════════════════════════════════════════════════
// SoccerColors — Domain-specific semantic color system
// Access via: LocalSoccerColors.current.xxx
// ═══════════════════════════════════════════════════════════════════════════════

data class SoccerColors(
    // ── Match Status ──────────────────────────────────────────────────────────
    val liveRed: Color = LiveRed,
    val matchWin: Color = Color(0xFF2EA64F),
    val matchDraw: Color = Color(0xFF9E9E9E),
    val matchLoss: Color = Color(0xFFE53935),
    val winnerText: Color = WinnerText,
    val loserText: Color = LoserText,

    // ── Zone Colors (League Table) ────────────────────────────────────────────
    val zoneChampionsLeague: Color = Color(0xFF0D47A1),
    val zoneEuropaLeague: Color    = Color(0xFFE65100),
    val zoneConferenceLeague: Color= Color(0xFF2E7D32),
    val zoneRelegation: Color      = Color(0xFFB71C1C),

    // ── Home / Away ──────────────────────────────────────────────────────────
    val homeColor: Color = BrandGreenMedium,
    val awayColor: Color = AccentNeonOrange,
    /** Home color for stats bars (SharedTabComponents HomeColor replacement) */
    val statsHome: Color = Color(0xFF00B050),
    /** Away color for stats bars (SharedTabComponents AwayColor replacement) */
    val statsAway: Color = Color(0xFF2B44FF),

    // ── Football Cards ────────────────────────────────────────────────────────
    val yellowCard: Color = Color(0xFFFBC02D),
    val redCard: Color = Color(0xFFD32F2F),

    // ── Substitution ─────────────────────────────────────────────────────────
    val subIn: Color = Color(0xFF388E3C),
    val subOut: Color = Color(0xFFD32F2F),

    // ── Pitch (fixed — always on green background) ───────────────────────────
    val pitchGreen: Color = Color(0xFF1E5A22),
    val pitchGreenDark: Color = Color(0xFF1B4D22),
    val pitchLine: Color = Color(0xFF3C7F62),
    val pitchOverlay: Color = Color(0xFF1B5E20),
    val pitchHomeMarker: Color = Color(0xFF1E88E5),
    val pitchAwayMarker: Color = Color(0xFFE53935),

    // ── Player Rating ────────────────────────────────────────────────────────
    val ratingExcellent: Color = Color(0xFF3248F3),  // 8.0+
    val ratingGood: Color = Color(0xFF00C224),        // 7.0-7.9
    val ratingAverage: Color = Color(0xFFEB7D07),     // 6.0-6.9
    val ratingPoor: Color = Color(0xFFDA0C00),        // < 6.0

    // ── Player Position ──────────────────────────────────────────────────────
    val positionGK: Color = Color(0xFFFFB300),
    val positionDef: Color = Color(0xFF1E88E5),
    val positionMid: Color = Color(0xFF43A047),
    val positionFwd: Color = Color(0xFFE53935),
    val positionDefault: Color = Color(0xFF546E7A),

    // ── Medals (Top Scorer) ──────────────────────────────────────────────────
    val medalGold: Color = FavoriteGold,
    val medalSilver: Color = Color(0xFFB8B8B8),
    val medalBronze: Color = Color(0xFFCD7F32),

    // ── Favorites ────────────────────────────────────────────────────────────
    val favoriteActive: Color = FavoriteGold,
    val favoriteInactive: Color = Color(0xFFBDBDBD),

    // ── Status (Predictions in Profile) ──────────────────────────────────────
    val statusSuccessBg: Color = Color(0xFFE8F5E9),
    val statusSuccessText: Color = Color(0xFF2E7D32),
    val statusFailedBg: Color = Color(0xFFFFEBEE),
    val statusFailedText: Color = Color(0xFFC62828),
    val statusPendingBg: Color = Color(0xFFFFF3E0),
    val statusPendingText: Color = Color(0xFFEF6C00),

    // ── League Table ─────────────────────────────────────────────────────────
    val tableHighlightBg: Color = Color(0xFFE3F2FD),

    // ── Brand & Accent (for components outside M3 colorScheme) ───────────────
    val accentEmerald: Color = AccentNeonMint,
    val accentOrange: Color = AccentNeonOrange,
    val brandDark: Color = BrandNavy,
    val brandDarkMid: Color = BrandNavyMid,
    val brandLight: Color = BrandGreenLight,

    // ── Profile Menu Icon Colors ─────────────────────────────────────────────
    val profileIconPurple: Color = Color(0xFF673AB7),
    val profileIconPink: Color = Color(0xFFE91E63),
    val profileIconOrange: Color = Color(0xFFFB8C00),
    val profileIconBlue: Color = Color(0xFF2196F3),
    val profileIconAmber: Color = Color(0xFFFFB300),

    // ── Comment Avatar Colors ────────────────────────────────────────────────
    val avatarColors: List<Color> = listOf(
        Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFF57C00),
        Color(0xFF7B1FA2), Color(0xFFD32F2F), Color(0xFF00796B)
    ),

    // ── Login Screen ─────────────────────────────────────────────────────────
    val loginGradient: List<Color> = listOf(
        Color(0xFF1E3A8A), Color(0xFF3B82F6), Color(0xFFEEF2FF)
    ),
    val loginCardBorder: Color = Color(0xFFE2E8F0),
    val loginDividerText: Color = Color(0xFF475569),
    val googleBrandRed: Color = Color(0xFFDB4437),
    val loginDarkText: Color = Color(0xFF1F1F1F),

    // ── Goal/Assist highlight ────────────────────────────────────────────────
    val goalIcon: Color = Color(0xFF4CAF50),
    val assistText: Color = Color(0xFF1E88E5),
)

// ═══════════════════════════════════════════════════════════════════════════════
// Light & Dark Variants
// ═══════════════════════════════════════════════════════════════════════════════

val LightSoccerColors = SoccerColors()

val DarkSoccerColors = SoccerColors(
    // Match status — brighter for dark backgrounds
    matchWin = Color(0xFF4ADE80),
    matchDraw = Color(0xFFBDBDBD),
    matchLoss = Color(0xFFEF5350),
    winnerText = Color.White,
    loserText = Color.LightGray,

    // Zones — lighter for dark mode
    zoneChampionsLeague = Color(0xFF64B5F6),
    zoneEuropaLeague = Color(0xFFFFB74D),
    zoneConferenceLeague = Color(0xFF81C784),
    zoneRelegation = Color(0xFFE57373),

    // Status — dark mode variants
    statusSuccessBg = Color(0xFF1B3A1B),
    statusSuccessText = Color(0xFF81C784),
    statusFailedBg = Color(0xFF3A1B1B),
    statusFailedText = Color(0xFFE57373),
    statusPendingBg = Color(0xFF3A2E1B),
    statusPendingText = Color(0xFFFFB74D),

    // League table
    tableHighlightBg = Color(0xFF1E3A8A).copy(alpha = 0.4f),

    // Favorites
    favoriteInactive = Color(0xFF757575),

    // Login — dark gradient
    loginGradient = listOf(
        Color(0xFF080C14), Color(0xFF0F172A), Color(0xFF1E293B)
    ),
    loginCardBorder = Color(0xFF334155),
    loginDividerText = Color(0xFF64748B),
    loginDarkText = Color(0xFFE2E8F0),
)

val LocalSoccerColors = compositionLocalOf { LightSoccerColors }
