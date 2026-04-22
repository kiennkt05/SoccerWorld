package com.example.soccerworld.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.util.Injection

class LiveMatchesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = Injection.provideFootballRepository(applicationContext)
        val leagueId = repository.getSelectedLeagueId()
        val result = repository.getAllFixtureOfLeague(
            leagueId = leagueId,
            status = "IN_PLAY",
            forceRefresh = true
        )

        return when (result) {
            is DataResult.Success -> {
                val liveCount = result.data.matches?.size ?: 0
                Log.d("LiveMatchesWorker", "Live polling run complete, liveCount=$liveCount")
                if (liveCount > 0) {
                    LivePollingScheduler.start(applicationContext)
                } else {
                    LivePollingScheduler.stop(applicationContext)
                }
                Result.success()
            }
            is DataResult.Error -> {
                Log.e("LiveMatchesWorker", "Live polling failed: ${result.message}")
                Result.retry()
            }
            DataResult.Loading -> Result.retry()
        }
    }
}
