package com.example.soccerworld.model.topscorer

import androidx.room.Entity
import androidx.room.PrimaryKey

// Đặt tên bảng rõ ràng
@Entity(tableName = "topscorer_table")
data class TopScorerEntity(
    @PrimaryKey
    val playerId: String,      // ID cầu thủ làm khóa chính
    val playerName: String,    // Tên cầu thủ
    val teamName: String,      // Tên đội bóng
    val goals: Int,            // Số bàn thắng
    val imageUrl: String? = null
)