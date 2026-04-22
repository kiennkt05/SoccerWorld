package com.example.soccerworld.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soccerworld.data.local.entity.FavoriteMatchEntity
import com.example.soccerworld.data.local.entity.EspnMatchEnrichmentEntity
import com.example.soccerworld.data.local.entity.EspnPlayerProfileEntity
import com.example.soccerworld.data.local.entity.EspnTeamProfileEntity
import com.example.soccerworld.data.local.entity.IdBridgeEntity
import com.example.soccerworld.data.local.entity.PlayerMediaEntity
import com.example.soccerworld.data.local.entity.TeamMediaEntity
import com.example.soccerworld.model.topscorer.TopScorerEntity
import kotlinx.coroutines.flow.Flow

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdBridge(item: IdBridgeEntity)

    @Query("SELECT * FROM id_bridge WHERE footballDataId = :footballDataId LIMIT 1")
    suspend fun getIdBridge(footballDataId: Int): IdBridgeEntity?

    @Query("SELECT * FROM id_bridge WHERE footballDataId = :footballDataId AND entityType = :entityType LIMIT 1")
    suspend fun getIdBridgeByType(footballDataId: Int, entityType: String): IdBridgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMedia(item: TeamMediaEntity)

    @Query("SELECT * FROM team_media WHERE footballDataTeamId = :teamId LIMIT 1")
    suspend fun getTeamMedia(teamId: Int): TeamMediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerMedia(item: PlayerMediaEntity)

    @Query("SELECT * FROM player_media WHERE footballDataPlayerId = :playerId LIMIT 1")
    suspend fun getPlayerMedia(playerId: Int): PlayerMediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEspnMatchEnrichment(item: EspnMatchEnrichmentEntity)

    @Query("SELECT * FROM espn_match_enrichment WHERE footballDataMatchId = :matchId LIMIT 1")
    suspend fun getEspnMatchEnrichment(matchId: Int): EspnMatchEnrichmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEspnTeamProfile(item: EspnTeamProfileEntity)

    @Query("SELECT * FROM espn_team_profile WHERE espnTeamId = :espnTeamId LIMIT 1")
    suspend fun getEspnTeamProfile(espnTeamId: String): EspnTeamProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEspnPlayerProfile(item: EspnPlayerProfileEntity)

    @Query("SELECT * FROM espn_player_profile WHERE espnPlayerId = :espnPlayerId LIMIT 1")
    suspend fun getEspnPlayerProfile(espnPlayerId: String): EspnPlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteMatchEntity)

    @Query("DELETE FROM favorite_matches WHERE matchId = :matchId")
    suspend fun deleteFavorite(matchId: Int)

    @Query("SELECT * FROM favorite_matches ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteMatchEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_matches WHERE matchId = :matchId)")
    fun observeIsFavorite(matchId: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_matches WHERE matchId = :matchId)")
    suspend fun isFavorite(matchId: Int): Boolean
}