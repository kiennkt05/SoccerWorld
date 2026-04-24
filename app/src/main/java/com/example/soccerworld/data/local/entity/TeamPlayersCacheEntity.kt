package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_players_cache")
data class TeamPlayersCacheEntity(
    @PrimaryKey val teamId: String,
    val payloadJson: String,
    val updatedAt: Long
)
