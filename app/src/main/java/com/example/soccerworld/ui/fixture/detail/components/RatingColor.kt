package com.example.soccerworld.ui.fixture.detail.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.soccerworld.ui.theme.LocalSoccerColors

@Composable
fun getRatingColor(rating: Double): Color {
    val soccerColors = LocalSoccerColors.current
    return when {
        rating >= 8.0 -> soccerColors.ratingExcellent
        rating >= 7.0 -> soccerColors.ratingGood
        rating >= 6.0 -> soccerColors.ratingAverage
        else -> soccerColors.ratingPoor
    }
}

@Composable
fun getRatingColor(ratingStr: String?): Color {
    val rating = ratingStr?.toDoubleOrNull() ?: 0.0
    return getRatingColor(rating)
}
