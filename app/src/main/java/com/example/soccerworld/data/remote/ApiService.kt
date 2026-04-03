package com.example.soccerworld.data.remote

import com.example.soccerworld.model.fixture.FixtureResponse
import com.example.soccerworld.model.h2h.H2HResponse
import com.example.soccerworld.model.leaguetable.LeagueTableResponse
import com.example.soccerworld.model.player.PlayerResponse
import com.example.soccerworld.model.statistic.StatisticsResponse
import com.example.soccerworld.model.team.TeamResponse
import com.example.soccerworld.model.topscorer.TopScorerResponse
import com.example.soccerworld.model.transfer.TransferResponse
import com.example.soccerworld.util.Constant
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface ApiService {

    @Headers(Constant.API_KEY)
    @GET(Constant.GET_LEAGUE_TABLE)
    fun getLeagueTable(@Path("league_id") leagueId: Int): Single<LeagueTableResponse>

    @Headers(Constant.API_KEY)
    @GET(Constant.GET_TOP_SCORERS)
    fun getTopScorers(@Path("league_id") leagueId: Int): Single<TopScorerResponse>

    @Headers(Constant.API_KEY)
    @GET(Constant.GET_ALL_TEAMS_OF_LEAGUE)
    fun getAllTeamsOfLeague(@Path("league_id") leagueId: Int): Single<TeamResponse>

    @Headers(Constant.API_KEY)
    @GET(Constant.GET_ALL_PLAYERS_OF_TEAM)
    fun getAllPlayersOfTeam(@Path("team_id") teamId: Int): Single<PlayerResponse>

    @Headers(Constant.API_KEY)
    @GET(Constant.GET_ALL_TRANSFERS_OF_TEAM)
    fun getAllTransfersOfTeam(@Path("team_id") teamId: Int): Single<TransferResponse>

    @Headers(Constant.API_KEY)
    @GET(Constant.GET_ALL_FIXTURE_OF_LEAGUE)
    fun getAllFixtureOfLeague(@Path("league_id") leagueId: Int): Single<FixtureResponse>

    @Headers(Constant.API_KEY)
    @GET(Constant.GET_ALL_H2H_ITEMS)
    fun getAllH2hItems(@Path("home_team_id") homeTeamId: Int, @Path("away_team_id") awayTeamId: Int): Single<H2HResponse>

    @Headers(Constant.API_KEY)
    @GET(Constant.GET_FIXTURE_STATISTICS)
    fun getFixtureStatistics(@Path("fixture_id") fixtureId: Int): Single<StatisticsResponse>

}