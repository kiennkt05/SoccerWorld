package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_matches")
data class FavoriteMatchEntity(
    @PrimaryKey val matchId: Int,
    val leagueCode: String?,
    val utcDate: String?,
    val homeTeamId: Int?,
    val homeTeamName: String?,
    val homeTeamCrest: String?,
    val awayTeamId: Int?,
    val awayTeamName: String?,
    val awayTeamCrest: String?,
    val status: String?,
    val savedAt: Long
)
