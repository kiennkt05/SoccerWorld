package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fixtures_cache")
data class FixturesCacheEntity(
    @PrimaryKey val queryKey: String,
    val payloadJson: String,
    val updatedAt: Long
)
