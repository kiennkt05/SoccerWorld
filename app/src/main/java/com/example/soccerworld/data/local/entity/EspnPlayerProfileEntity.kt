package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "espn_player_profile")
data class EspnPlayerProfileEntity(
    @PrimaryKey val espnPlayerId: String,
    val footballDataPlayerId: Int?,
    val playerName: String?,
    val imageUrl: String?,
    val payloadJson: String?,
    val lastUpdated: Long
)
