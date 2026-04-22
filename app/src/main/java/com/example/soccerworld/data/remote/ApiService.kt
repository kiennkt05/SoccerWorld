package com.example.soccerworld.data.remote

import com.example.soccerworld.model.fixture.FixtureResponse
import com.example.soccerworld.model.h2h.H2HResponse
import com.example.soccerworld.model.leaguetable.LeagueTableResponse
import com.example.soccerworld.model.player.PlayerResponse
import com.example.soccerworld.model.statistic.StatisticsResponse
import com.example.soccerworld.model.team.TeamResponse
import com.example.soccerworld.model.topscorer.TopScorerResponse
import com.example.soccerworld.util.Constant
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // 1. BẢNG XẾP HẠNG (Standings)
    @GET(Constant.GET_LEAGUE_TABLE)
    suspend fun getLeagueTable(
        @Header("X-Auth-Token") token: String,
        @Path("league_id") leagueId: String,
        @Query("season") season: String? = null,     // VD: "2023"
        @Query("matchday") matchday: Int? = null,    // VD: 10 (Vòng đấu thứ 10)
        @Query("date") date: String? = null          // VD: "2023-10-25" (Định dạng YYYY-MM-DD)
    ): LeagueTableResponse

    // 2. VUA PHÁ LƯỚI (Top Scorers)
    @GET(Constant.GET_TOP_SCORERS)
    suspend fun getTopScorers(
        @Header("X-Auth-Token") token: String,
        @Path("league_id") leagueId: String,
        @Query("season") season: String? = null,     // VD: "2023"
        @Query("limit") limit: Int? = null           // VD: 10 (Lấy top 10 người)
    ): TopScorerResponse

    // 3. DANH SÁCH ĐỘI BÓNG (Teams of Competition)
    @GET(Constant.GET_ALL_TEAMS_OF_LEAGUE)
    suspend fun getAllTeamsOfLeague(
        @Header("X-Auth-Token") token: String,
        @Path("league_id") leagueId: String,
        @Query("season") season: String? = null      // VD: "2023"
    ): TeamResponse

    // 4. CHI TIẾT 1 ĐỘI BÓNG / CẦU THỦ (Team) -> KHÔNG CÓ FILTER
    @GET(Constant.GET_ALL_PLAYERS_OF_TEAM)
    suspend fun getAllPlayersOfTeam(
        @Header("X-Auth-Token") token: String,
        @Path("id") teamId: Int
    ): PlayerResponse

    // 5. LỊCH THI ĐẤU (Matches of Competition) -> CÓ NHIỀU FILTER NHẤT
    @GET(Constant.GET_ALL_FIXTURE_OF_LEAGUE)
    suspend fun getAllFixtureOfLeague(
        @Header("X-Auth-Token") token: String,
        @Path("league_id") leagueId: String,
        @Query("dateFrom") dateFrom: String? = null, // VD: "2023-11-01"
        @Query("dateTo") dateTo: String? = null,     // VD: "2023-11-30"
        @Query("stage") stage: String? = null,       // VD: "GROUP_STAGE", "FINAL"
        @Query("status") status: String? = null,     // VD: "SCHEDULED" (Sắp đá), "FINISHED" (Đã đá xong)
        @Query("matchday") matchday: Int? = null,    // VD: 15
        @Query("group") group: String? = null,       // VD: "GROUP_A"
        @Query("season") season: String? = null      // VD: "2023"
    ): FixtureResponse

    // 6. LỊCH SỬ ĐỐI ĐẦU (Head to Head)
    @GET(Constant.GET_ALL_H2H_ITEMS)
    suspend fun getAllH2hItems(
        @Header("X-Auth-Token") token: String,
        @Path("id") fixtureId: Int,                  // ID của trận đấu
        @Query("limit") limit: Int? = null,          // VD: 5 (Lấy 5 trận đối đầu gần nhất)
        @Query("dateFrom") dateFrom: String? = null, // VD: "2020-01-01"
        @Query("dateTo") dateTo: String? = null,
        @Query("competitions") competitions: String? = null // VD: "PL,CL" (Chỉ tính đối đầu ở Ngoại hạng và C1)
    ): H2HResponse

    // 7. CHI TIẾT 1 TRẬN ĐẤU (Match Detail) -> KHÔNG CÓ FILTER
    @GET(Constant.GET_FIXTURE_STATISTICS)
    suspend fun getFixtureStatistics(
        @Header("X-Auth-Token") token: String,
        @Path("fixture_id") fixtureId: Int
    ): StatisticsResponse
}