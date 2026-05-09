package com.example.soccerworld.ui.fixture.detail.components

import androidx.compose.ui.graphics.Color

fun getRatingColor(rating: Double): Color {
    return when {
        rating >= 8.0 -> Color(0xFF3248F3) // Standout Blue
        rating >= 7.0 -> Color(0xFF00C224) // Strong Green (Sofascore style)
        rating >= 6.0 -> Color(0xFFEB7D07) // Middling Yellow/Orange
        else -> Color(0xFFDA0C00)          // Poor Red
    }
}

fun getRatingColor(ratingStr: String?): Color {
    val rating = ratingStr?.toDoubleOrNull() ?: 0.0
    return getRatingColor(rating)
}
