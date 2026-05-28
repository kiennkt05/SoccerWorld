package com.example.soccerworld.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.NotificationHelper
import kotlinx.coroutines.flow.first

class LiveMatchesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "LiveMatchesWorker"
        private fun scoreKey(matchId: String)  = "live_score_$matchId"
        private fun statusKey(matchId: String) = "live_status_$matchId"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "LiveMatchesWorker starting poll...")
        val repository = Injection.provideFootballRepository(applicationContext)
        
        // 1. Get favorite matches from database
        val db = com.example.soccerworld.data.local.FootballDatabase.invoke(applicationContext)
        val favoritesFlow = db.footballDao().getAllFavorites()
        val favoriteIds = favoritesFlow.first().map { it.matchId }.toSet()

        if (favoriteIds.isEmpty()) {
            Log.d(TAG, "No favorite matches found, stopping polling.")
            LivePollingScheduler.stop(applicationContext)
            return Result.success()
        }

        val prefs = applicationContext.getSharedPreferences("live_score_cache", Context.MODE_PRIVATE)
        var anyLive = false

        // 2. Check each favorite match individually
        for (matchId in favoriteIds) {
            // Using getFixtureStatistics because it directly calls getEventData without caching
            val statsResult = repository.getFixtureStatistics(matchId)
            
            if (statsResult is DataResult.Success) {
                val stats = statsResult.data
                val status = stats.status ?: ""
                val homeTeam = stats.homeTeam?.shortName ?: stats.homeTeam?.name ?: "Home"
                val awayTeam = stats.awayTeam?.shortName ?: stats.awayTeam?.name ?: "Away"
                val homeScore = stats.score?.fullTime?.home ?: 0
                val awayScore = stats.score?.fullTime?.away ?: 0
                
                val currentScore = "$homeScore-$awayScore"
                val cachedScore = prefs.getString(scoreKey(matchId), null)
                val cachedStatus = prefs.getString(statusKey(matchId), null)

                if (status == "IN_PLAY" || status == "PAUSED") {
                    anyLive = true
                    
                    if (cachedScore != currentScore) {
                        Log.d(TAG, "Score update for favorite: $matchId | $cachedScore -> $currentScore")
                        if (cachedScore != null || homeScore > 0 || awayScore > 0) {
                            NotificationHelper.sendLiveScoreNotification(
                                context = applicationContext,
                                matchId = matchId,
                                homeTeam = homeTeam,
                                awayTeam = awayTeam,
                                homeScore = homeScore,
                                awayScore = awayScore
                            )
                        }
                    }
                    
                    prefs.edit()
                        .putString(scoreKey(matchId), currentScore)
                        .putString(statusKey(matchId), status)
                        .apply()
                        
                } else if (status == "FINISHED") {
                    // Match just finished
                    if (cachedStatus == "IN_PLAY" || cachedStatus == "PAUSED" || cachedStatus == "HALF_TIME") {
                        Log.d(TAG, "Favorite match finished: $matchId")
                        NotificationHelper.sendMatchResultNotification(
                            context = applicationContext,
                            matchId = matchId,
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            homeScore = homeScore,
                            awayScore = awayScore,
                            winner = null // We don't have winner easily from stats, that's fine
                        )
                        
                        prefs.edit()
                            .remove(scoreKey(matchId))
                            .remove(statusKey(matchId))
                            .apply()
                    }
                }
            } else {
                Log.e(TAG, "Failed to fetch stats for favorite match: $matchId")
            }
        }

        // If there is any live match among favorites, continue polling
        if (anyLive) {
            Log.d(TAG, "Live matches found among favorites. Rescheduling...")
            LivePollingScheduler.start(applicationContext)
        } else {
            Log.d(TAG, "No live matches among favorites. Polling can sleep until triggered.")
            LivePollingScheduler.stop(applicationContext)
        }

        return Result.success()
    }
}

