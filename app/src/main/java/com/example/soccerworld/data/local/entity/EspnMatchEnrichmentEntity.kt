package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "espn_match_enrichment")
data class EspnMatchEnrichmentEntity(
    @PrimaryKey val footballDataMatchId: Int,
    val espnEventId: String,
    val payloadJson: String,
    val status: String?,
    val lastUpdated: Long
)
