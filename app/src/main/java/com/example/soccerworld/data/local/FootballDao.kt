package com.example.soccerworld.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soccerworld.model.topscorer.TopScorerEntity

@Dao
interface FootballDao {

    // Thay vararg bằng List cho dễ dùng, thêm OnConflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopScorers(scorers: List<TopScorerEntity>)

    // Đổi 'toplam_gol' thành 'goals' cho chuẩn với Entity mới
    @Query("SELECT * FROM topscorer_table ORDER BY goals DESC")
    suspend fun getTopScorers(): List<TopScorerEntity>

    @Query("DELETE FROM topscorer_table")
    suspend fun clearTopScorers()
}