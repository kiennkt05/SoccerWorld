package com.example.soccerworld.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soccerworld.data.local.entity.FixturesCacheEntity
import com.example.soccerworld.data.local.entity.FavoriteMatchEntity
import com.example.soccerworld.data.local.entity.MatchDetailCacheEntity
import com.example.soccerworld.data.local.entity.StandingsCacheEntity
import com.example.soccerworld.data.local.entity.TeamPlayersCacheEntity
import com.example.soccerworld.data.local.entity.TeamsCacheEntity
import com.example.soccerworld.model.topscorer.TopScorerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FootballDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopScorers(scorers: List<TopScorerEntity>)

    @Query("SELECT * FROM topscorer_table WHERE leagueId = :leagueId ORDER BY goals DESC")
    suspend fun getTopScorers(leagueId: String): List<TopScorerEntity>

    @Query("DELETE FROM topscorer_table WHERE leagueId = :leagueId")
    suspend fun clearTopScorers(leagueId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStandingsCache(item: StandingsCacheEntity)

    @Query("SELECT * FROM standings_cache WHERE leagueId = :leagueId LIMIT 1")
    suspend fun getStandingsCache(leagueId: String): StandingsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFixturesCache(item: FixturesCacheEntity)

    @Query("SELECT * FROM fixtures_cache WHERE queryKey = :queryKey LIMIT 1")
    suspend fun getFixturesCache(queryKey: String): FixturesCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeamsCache(item: TeamsCacheEntity)

    @Query("SELECT * FROM teams_cache WHERE leagueId = :leagueId LIMIT 1")
    suspend fun getTeamsCache(leagueId: String): TeamsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeamPlayersCache(item: TeamPlayersCacheEntity)

    @Query("SELECT * FROM team_players_cache WHERE teamId = :teamId LIMIT 1")
    suspend fun getTeamPlayersCache(teamId: String): TeamPlayersCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMatchDetailCache(item: MatchDetailCacheEntity)

    @Query("SELECT * FROM match_detail_cache WHERE fixtureId = :fixtureId LIMIT 1")
    suspend fun getMatchDetailCache(fixtureId: String): MatchDetailCacheEntity?

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