package com.example.soccerworld.data

import com.example.soccerworld.data.local.FootballDao
import com.example.soccerworld.data.remote.ApiService
import com.example.soccerworld.model.topscorer.TopScorerEntity
import com.example.soccerworld.util.CustomSharedPreferences
import com.example.soccerworld.util.toEntityList

class FootballRepository(
    private val apiService: ApiService,
    private val dao: FootballDao,
    private val customPreferences: CustomSharedPreferences
) {
    private val refreshTime = 24L * 60 * 60 * 1000 * 1000 * 1000L

    // Lấy LeagueId đang chọn
    fun getSelectedLeagueId(): String = customPreferences.getLeagueId() ?: "2021"

    // 1. BẢNG XẾP HẠNG
    suspend fun getLeagueTable(
        leagueId: String,
        season: String? = null,
        matchday: Int? = null,
        date: String? = null
    ) = apiService.getLeagueTable(leagueId, season, matchday, date)

    // 2. VUA PHÁ LƯỚI
    suspend fun getTopScorers(leagueId: String): List<TopScorerEntity> {
        val updateTime = customPreferences.getTime() ?: 0L
        val isCacheValid = (System.nanoTime() - updateTime) < refreshTime

        if (isCacheValid) {
            val localData = dao.getTopScorers()
            if (localData.isNotEmpty()) {
                return localData // Trả về data từ ổ cứng
            }
        }

        // Nếu hết hạn hoặc ổ cứng trống -> Gọi mạng
        val response = apiService.getTopScorers(leagueId)
        val entities = response.toEntityList()

        // Lưu vào ổ cứng & cập nhật thời gian
        dao.clearTopScorers()
        dao.insertTopScorers(entities)
        customPreferences.saveTime(System.nanoTime())

        return entities // Trả về data mới nhất
    }

    // 3. DANH SÁCH ĐỘI BÓNG
    suspend fun getAllTeamsOfLeague(
        leagueId: String,
        season: String? = null
    ) = apiService.getAllTeamsOfLeague(leagueId, season)

    // 4. CHI TIẾT ĐỘI BÓNG (Không có filter)
    suspend fun getAllPlayersOfTeam(
        teamId: Int
    ) = apiService.getAllPlayersOfTeam(teamId)

    // 5. LỊCH THI ĐẤU
    suspend fun getAllFixtureOfLeague(
        leagueId: String,
        dateFrom: String? = null,
        dateTo: String? = null,
        stage: String? = null,
        status: String? = null,
        matchday: Int? = null,
        group: String? = null,
        season: String? = null
    ) = apiService.getAllFixtureOfLeague(leagueId, dateFrom, dateTo, stage, status, matchday, group, season)

    // 6. LỊCH SỬ ĐỐI ĐẦU
    suspend fun getAllH2hItems(
        fixtureId: Int,
        limit: Int? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        competitions: String? = null
    ) = apiService.getAllH2hItems(fixtureId, limit, dateFrom, dateTo, competitions)

    // 7. CHI TIẾT TRẬN ĐẤU (Không có filter)
    suspend fun getFixtureStatistics(
        fixtureId: Int
    ) = apiService.getFixtureStatistics(fixtureId)
}