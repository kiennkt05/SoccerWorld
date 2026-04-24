package com.example.soccerworld.model.topscorer

import androidx.room.Entity

// Đặt tên bảng rõ ràng
@Entity(tableName = "topscorer_table", primaryKeys = ["leagueId", "playerId"])
data class TopScorerEntity(
    val leagueId: String = "",
    val playerId: String,      // ID cầu thủ làm khóa chính
    val playerName: String,    // Tên cầu thủ
    val teamName: String,      // Tên đội bóng
    val goals: Int,            // Số bàn thắng
    val imageUrl: String? = null
)