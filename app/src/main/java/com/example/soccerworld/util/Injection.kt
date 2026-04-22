package com.example.soccerworld.util

import android.content.Context
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.local.FootballDatabase
import com.example.soccerworld.data.remote.ApiClient
import com.example.soccerworld.data.remote.espn.EspnClient
import com.example.soccerworld.data.remote.sportsdb.SportsDbClient
import com.example.soccerworld.data.repository.EspnEnrichmentRepository
import com.example.soccerworld.data.repository.MediaRepository

// Dùng object để biến nó thành Singleton (Chỉ có 1 nhà máy duy nhất trong toàn app)
object Injection {

    // Hàm này làm nhiệm vụ lắp ráp các phụ kiện và xuất xưởng Repository
    fun provideFootballRepository(context: Context): FootballRepository {
        val apiService = ApiClient().api
        val dao = FootballDatabase.invoke(context).footballDao()
        val customPreferences = CustomSharedPreferences.invoke(context)
        val sportsDbApi = SportsDbClient().api
        val espnApi = EspnClient().api
        val mediaRepository = MediaRepository(sportsDbApi, dao)
        val espnEnrichmentRepository = EspnEnrichmentRepository(espnApi, dao)

        return FootballRepository(apiService, dao, customPreferences, mediaRepository, espnEnrichmentRepository)
    }
}