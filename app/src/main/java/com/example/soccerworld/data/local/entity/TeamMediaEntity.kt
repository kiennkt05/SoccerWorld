package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_media")
data class TeamMediaEntity(
    @PrimaryKey val footballDataTeamId: Int,
    val teamName: String,
    val badgeUrl: String?,
    val bannerUrl: String?,
    val lastUpdated: Long
)
