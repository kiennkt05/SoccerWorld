package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_media")
data class PlayerMediaEntity(
    @PrimaryKey val footballDataPlayerId: Int,
    val playerName: String,
    val thumbUrl: String?,
    val cutoutUrl: String?,
    val lastUpdated: Long
)
