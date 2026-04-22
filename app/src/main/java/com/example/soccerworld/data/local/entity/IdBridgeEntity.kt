package com.example.soccerworld.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "id_bridge")
data class IdBridgeEntity(
    @PrimaryKey val footballDataId: Int,
    val entityType: String,
    val espnId: String?,
    val sportsDbId: String?,
    val canonicalName: String,
    val resolvedAt: Long
)
