package com.example.soccerworld.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Shape system for SoccerWorld.
 *
 * Usage: MaterialTheme.shapes.small, .medium, .large, etc.
 *
 * Mapping guide:
 *   extraSmall (4dp)  → chips, small badges, card indicators
 *   small (8dp)       → buttons, text fields, small cards
 *   medium (12dp)     → cards, dialogs, list items
 *   large (16dp)      → large cards, bottom sheets
 *   extraLarge (28dp) → full-screen sheets, FABs
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
