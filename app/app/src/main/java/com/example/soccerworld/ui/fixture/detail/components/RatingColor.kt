package com.example.soccerworld.ui.fixture.detail.components

import androidx.compose.ui.graphics.Color

fun getRatingColor(rating: Double): Color {
    return when {
        rating >= 8.0 -> Color(0xFF1E88E5) // Standout Blue
        rating >= 7.0 -> Color(0xFF2E7D32) // Strong Green (Sofascore style)
        rating >= 6.0 -> Color(0xFFE65100) // Middling Yellow/Orange (Sofascore uses around 6.0-6.9)
        else -> Color(0xFFC62828)          // Poor Red
    }
}

fun getRatingColor(ratingStr: String?): Color {
    val rating = ratingStr?.toDoubleOrNull() ?: 0.0
    return getRatingColor(rating)
}
