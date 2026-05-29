package com.example.soccerworld.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

class SingleMatchWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SingleMatchWorker"
        private fun scoreKey(matchId: String)  = "live_score_$matchId"
        private fun statusKey(matchId: String) = "live_status_$matchId"
    }

    override suspend fun doWork(): Result {
        val matchId = inputData.getString("matchId") ?: return Result.failure()
        
        Log.i(TAG, "================================================")
        Log.i(TAG, "SingleMatchWorker started for match: $matchId")
        
        val repository = Injection.provideFootballRepository(applicationContext)
        val db = com.example.soccerworld.data.local.FootballDatabase.invoke(applicationContext)
        val prefs = applicationContext.getSharedPreferences("live_score_cache", Context.MODE_PRIVATE)

        val statsResult = repository.getFixtureStatistics(matchId)
        
        if (statsResult is DataResult.Success) {
            val stats = statsResult.data
            val status = stats.status ?: "UNKNOWN"
            val homeTeam = stats.homeTeam?.shortName ?: stats.homeTeam?.name ?: "Home"
            val awayTeam = stats.awayTeam?.shortName ?: stats.awayTeam?.name ?: "Away"
            val homeScore = stats.score?.fullTime?.home ?: 0
            val awayScore = stats.score?.fullTime?.away ?: 0
            val utcDate = stats.utcDate
            
            val currentScore = "$homeScore-$awayScore"
            Log.i(TAG, "[FAVORITE MATCH $matchId] Teams: $homeTeam vs $awayTeam | Status: $status | Score: $currentScore")
            
            val cachedScore = prefs.getString(scoreKey(matchId), null)
            val cachedStatus = prefs.getString(statusKey(matchId), null)

            if (status == "IN_PLAY" || status == "PAUSED") {
                if (cachedScore != currentScore) {
                    Log.w(TAG, ">>> GOAL DETECTED for $matchId! Score changed from $cachedScore to $currentScore. Triggering local notification...")
                    if (cachedScore != null || homeScore > 0 || awayScore > 0) {
                        NotificationHelper.sendLiveScoreNotification(
                            context = applicationContext,
                            matchId = matchId,
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            homeScore = homeScore,
                            awayScore = awayScore
                        )
                    } else {
                        Log.i(TAG, "Match just started ($currentScore). Not spamming notification yet.")
                    }
                }
                
                prefs.edit()
                    .putString(scoreKey(matchId), currentScore)
                    .putString(statusKey(matchId), status)
                    .apply()
                    
                db.footballDao().updateFavoriteMatchScoreAndStatus(matchId, status, homeScore, awayScore)
                
                Log.i(TAG, "Match $matchId is still LIVE. Rescheduling in 5 minutes.")
                LivePollingScheduler.startForMatchId(applicationContext, matchId, 300_000L)
                
            } else if (status == "FINISHED") {
                if (cachedStatus == "IN_PLAY" || cachedStatus == "PAUSED" || cachedStatus == "HALF_TIME") {
                    Log.w(TAG, ">>> MATCH FINISHED for $matchId! Sending final result notification.")
                    NotificationHelper.sendMatchResultNotification(
                        context = applicationContext,
                        matchId = matchId,
                        homeTeam = homeTeam,
                        awayTeam = awayTeam,
                        homeScore = homeScore,
                        awayScore = awayScore,
                        winner = null
                    )
                    
                    prefs.edit()
                        .remove(scoreKey(matchId))
                        .remove(statusKey(matchId))
                        .apply()
                        
                    db.footballDao().updateFavoriteMatchScoreAndStatus(matchId, status, homeScore, awayScore)
                }
                Log.i(TAG, "Match $matchId finished. Stopping polling.")
                
            } else if (status == "SCHEDULED" || status == "TIMED") {
                val delayMs = calculateDelayMs(utcDate)
                val fifteenMinsMs = 15 * 60 * 1000L
                
                if (delayMs in 1L..fifteenMinsMs) {
                    val reminderKey = "reminder_sent_$matchId"
                    if (!prefs.getBoolean(reminderKey, false)) {
                        val minutesLeft = (delayMs / 60000L).toInt()
                        NotificationHelper.sendMatchReminderNotification(
                            context = applicationContext,
                            matchId = matchId,
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            minutesLeft = minutesLeft
                        )
                        prefs.edit().putBoolean(reminderKey, true).apply()
                        Log.i(TAG, "Sent 15 min reminder for match $matchId")
                    }
                    // Wake up at match start time
                    Log.i(TAG, "Match $matchId is SCHEDULED. Rescheduling worker to wake up in ${delayMs / 1000} seconds.")
                    LivePollingScheduler.startForMatchId(applicationContext, matchId, delayMs)
                } else if (delayMs > fifteenMinsMs) {
                    // Wake up exactly 15 minutes before the match starts so we can send the reminder
                    val wakeUpIn = delayMs - fifteenMinsMs
                    Log.i(TAG, "Match $matchId is > 15 mins away. Rescheduling worker to wake up in ${wakeUpIn / 1000} seconds for reminder.")
                    LivePollingScheduler.startForMatchId(applicationContext, matchId, wakeUpIn)
                } else {
                    // Match should have started (delayMs <= 0). Check again in a few minutes.
                    Log.i(TAG, "Match $matchId is SCHEDULED but time passed. Rescheduling in ${delayMs / 1000} seconds.")
                    LivePollingScheduler.startForMatchId(applicationContext, matchId, delayMs)
                }
            } else {
                // Other statuses (CANCELED, POSTPONED) - just stop polling or check back later
                Log.i(TAG, "Match $matchId has status $status. Will not schedule next poll.")
            }
        } else {
            Log.e(TAG, "[FAVORITE MATCH $matchId] - Failed to fetch stats. API Error.")
            val cachedStatus = prefs.getString(statusKey(matchId), null)
            if (cachedStatus == "IN_PLAY" || cachedStatus == "PAUSED" || cachedStatus == "HALF_TIME") {
                Log.i(TAG, "Match $matchId was previously $cachedStatus. Network error - retrying in 5 minutes.")
                LivePollingScheduler.startForMatchId(applicationContext, matchId, 300_000L)
            } else if (cachedStatus == "SCHEDULED") {
                // Should we retry if scheduled? Let's retry in 5 minutes
                Log.i(TAG, "Match $matchId was previously $cachedStatus. Network error - retrying in 5 minutes.")
                LivePollingScheduler.startForMatchId(applicationContext, matchId, 300_000L)
            }
        }

        Log.i(TAG, "================================================")
        return Result.success()
    }

    private fun calculateDelayMs(utcDateString: String?): Long {
        if (utcDateString == null) return 60_000L // Default 1 min if null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            val date = format.parse(utcDateString)
            if (date != null) {
                val delay = date.time - System.currentTimeMillis()
                // If negative (time passed but still SCHEDULED), check again in 5 minutes
                if (delay < 0) 300_000L else delay
            } else {
                60_000L
            }
        } catch (e: Exception) {
            60_000L
        }
    }
}
