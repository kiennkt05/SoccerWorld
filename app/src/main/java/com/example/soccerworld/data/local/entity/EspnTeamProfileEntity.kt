package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "espn_team_profile")
data class EspnTeamProfileEntity(
    @PrimaryKey val espnTeamId: String,
    val footballDataTeamId: Int?,
    val teamName: String?,
    val logoUrl: String?,
    val payloadJson: String?,
    val lastUpdated: Long
)
