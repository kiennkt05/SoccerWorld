package com.example.soccerworld.util

import android.content.Context
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.local.FootballDatabase
import com.example.soccerworld.data.remote.ApiClient

// Dùng object để biến nó thành Singleton (Chỉ có 1 nhà máy duy nhất trong toàn app)
object Injection {

    // Hàm này làm nhiệm vụ lắp ráp các phụ kiện và xuất xưởng Repository
    fun provideFootballRepository(context: Context): FootballRepository {
        val appContext = context.applicationContext
        val apiService = ApiClient.api
        val dao = FootballDatabase.invoke(appContext).footballDao()
        val customPreferences = CustomSharedPreferences.invoke(appContext)
        return FootballRepository(appContext, apiService, dao, customPreferences)
    }
}