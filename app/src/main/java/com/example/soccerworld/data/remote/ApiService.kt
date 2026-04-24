package com.example.soccerworld.data.remote

import com.example.soccerworld.data.remote.flashlive.EventDataResponse
import com.example.soccerworld.data.remote.flashlive.EventStatsResponse
import com.example.soccerworld.data.remote.flashlive.EventSummaryResponse
import com.example.soccerworld.data.remote.flashlive.EventsListResponse
import com.example.soccerworld.data.remote.flashlive.FixturesResponse
import com.example.soccerworld.data.remote.flashlive.H2HResponse
import com.example.soccerworld.data.remote.flashlive.LineupsResponse
import com.example.soccerworld.data.remote.flashlive.PlayerDataResponse
import com.example.soccerworld.data.remote.flashlive.SearchResult
import com.example.soccerworld.data.remote.flashlive.SquadResponse
import com.example.soccerworld.data.remote.flashlive.StandingsResponse
import com.example.soccerworld.data.remote.flashlive.TeamDataResponse
import com.example.soccerworld.data.remote.flashlive.TopScorersResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
    @GET("/v1/tournaments/standings")
    suspend fun getStandings(
        @Query("locale") locale: String,
        @Query("standing_type") type: String,
        @Query("tournament_stage_id") stageId: String,
        @Query("tournament_season_id") seasonId: String,
        @Query("page") page: Int? = null
    ): StandingsResponse

    @GET("/v1/tournaments/standings")
    suspend fun getTopScorers(
        @Query("locale") locale: String,
        @Query("standing_type") type: String = "top_scores",
        @Query("tournament_stage_id") stageId: String,
        @Query("tournament_season_id") seasonId: String
    ): TopScorersResponse

    @GET("/v1/events/list")
    suspend fun getEventsByDay(
        @Query("locale") locale: String,
        @Query("sport_id") sportId: Int,
        @Query("timezone") timezone: Int,
        @Query("indent_days") indentDays: Int
    ): EventsListResponse

    @GET("/v1/tournaments/fixtures")
    suspend fun getTournamentFixtures(
        @Query("locale") locale: String,
        @Query("tournament_stage_id") stageId: String,
        @Query("page") page: Int = 1
    ): FixturesResponse

    @GET("/v1/tournaments/results")
    suspend fun getTournamentResults(
        @Query("locale") locale: String,
        @Query("tournament_stage_id") stageId: String,
        @Query("page") page: Int = 1
    ): FixturesResponse

    @GET("/v1/events/data")
    suspend fun getEventData(
        @Query("locale") locale: String,
        @Query("event_id") eventId: String
    ): EventDataResponse

    @GET("/v1/events/summary")
    suspend fun getEventSummary(
        @Query("locale") locale: String,
        @Query("event_id") eventId: String
    ): EventSummaryResponse

    @GET("/v1/events/statistics")
    suspend fun getEventStats(
        @Query("locale") locale: String,
        @Query("event_id") eventId: String
    ): EventStatsResponse

    @GET("/v1/events/lineups")
    suspend fun getEventLineups(
        @Query("locale") locale: String,
        @Query("event_id") eventId: String
    ): LineupsResponse

    @GET("/v1/events/h2h")
    suspend fun getHeadToHead(
        @Query("locale") locale: String,
        @Query("event_id") eventId: String
    ): H2HResponse

    @GET("/v1/teams/data")
    suspend fun getTeamData(
        @Query("locale") locale: String,
        @Query("sport_id") sportId: Int,
        @Query("team_id") teamId: String
    ): TeamDataResponse

    @GET("/v1/teams/squad")
    suspend fun getTeamSquad(
        @Query("locale") locale: String,
        @Query("sport_id") sportId: Int,
        @Query("team_id") teamId: String
    ): SquadResponse

    @GET("/v1/players/data")
    suspend fun getPlayerData(
        @Query("locale") locale: String,
        @Query("sport_id") sportId: Int,
        @Query("player_id") playerId: String
    ): PlayerDataResponse

    @GET("/v1/search/multi-search")
    suspend fun multiSearch(
        @Query("locale") locale: String,
        @Query("query") query: String
    ): List<SearchResult>

    @GET("/v1/images/data")
    @Streaming
    suspend fun getImage(
        @Query("image_id") imageId: String
    ): Response<ResponseBody>
}