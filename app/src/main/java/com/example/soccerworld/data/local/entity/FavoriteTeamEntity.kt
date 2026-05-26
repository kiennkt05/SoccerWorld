package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_teams")
data class FavoriteTeamEntity(
    @PrimaryKey val teamId: String,
    val name: String,
    val logoUrl: String?,
    val country: String?,
    val savedAt: Long
)
