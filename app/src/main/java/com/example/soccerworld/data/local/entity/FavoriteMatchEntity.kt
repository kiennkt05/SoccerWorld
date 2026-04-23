package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_matches")
data class FavoriteMatchEntity(
    @PrimaryKey val matchId: String,
    val leagueCode: String?,
    val utcDate: String?,
    val homeTeamId: String?,
    val homeTeamName: String?,
    val homeTeamCrest: String?,
    val awayTeamId: String?,
    val awayTeamName: String?,
    val awayTeamCrest: String?,
    val status: String?,
    val savedAt: Long
)
