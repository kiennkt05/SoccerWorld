package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_players")
data class FavoritePlayerEntity(
    @PrimaryKey val playerId: String,
    val name: String,
    val imageUrl: String?,
    val nationality: String?,
    val position: String?,
    val savedAt: Long
)
