package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams_cache")
data class TeamsCacheEntity(
    @PrimaryKey val leagueId: String,
    val payloadJson: String,
    val updatedAt: Long
)
