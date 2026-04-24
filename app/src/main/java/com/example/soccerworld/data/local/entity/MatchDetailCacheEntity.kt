package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_detail_cache")
data class MatchDetailCacheEntity(
    @PrimaryKey val fixtureId: String,
    val payloadJson: String,
    val updatedAt: Long
)
