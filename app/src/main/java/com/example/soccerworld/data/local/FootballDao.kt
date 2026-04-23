package com.example.soccerworld.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soccerworld.data.local.entity.FavoriteMatchEntity
import com.example.soccerworld.model.topscorer.TopScorerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FootballDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopScorers(scorers: List<TopScorerEntity>)

    @Query("SELECT * FROM topscorer_table ORDER BY goals DESC")
    suspend fun getTopScorers(): List<TopScorerEntity>

    @Query("DELETE FROM topscorer_table")
    suspend fun clearTopScorers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteMatchEntity)

    @Query("DELETE FROM favorite_matches WHERE matchId = :matchId")
    suspend fun deleteFavorite(matchId: String)

    @Query("SELECT * FROM favorite_matches ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteMatchEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_matches WHERE matchId = :matchId)")
    fun observeIsFavorite(matchId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_matches WHERE matchId = :matchId)")
    suspend fun isFavorite(matchId: String): Boolean
}