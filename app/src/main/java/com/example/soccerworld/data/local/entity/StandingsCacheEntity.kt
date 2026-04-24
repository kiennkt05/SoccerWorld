package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "standings_cache")
data class StandingsCacheEntity(
    @PrimaryKey val leagueId: String,
    val payloadJson: String,
    val updatedAt: Long
)
