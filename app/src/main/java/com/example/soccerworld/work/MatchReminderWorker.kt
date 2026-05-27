package com.example.soccerworld.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.soccerworld.data.local.FootballDatabase
import com.example.soccerworld.util.FcmV1Sender
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MatchReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "MatchReminderWorker"
        private const val REMINDER_WINDOW_MINUTES = 15L
        private const val PAST_TOLERANCE_MINUTES  = 2L
    }

    override suspend fun doWork(): Result {
        return try {
            val db        = FootballDatabase.invoke(applicationContext)
            val favorites = db.footballDao().getAllFavorites().first()

            if (favorites.isEmpty()) {
                Log.d(TAG, "No favorites, skipping.")
                return Result.success()
            }

            val nowMs     = System.currentTimeMillis()
            val dateFormatZ = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val dateFormatNoZ = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            var sentCount = 0

            for (match in favorites) {
                val dateStr   = match.utcDate ?: continue
                val matchTimeMs = try {
                    val parsedDate = dateFormatZ.parse(dateStr) ?: dateFormatNoZ.parse(dateStr) ?: throw Exception("Parsed date is null")
                    parsedDate.time
                } catch (e: Exception) {
                    try {
                        val parsedDate = dateFormatNoZ.parse(dateStr) ?: throw Exception("Parsed date is null")
                        parsedDate.time
                    } catch (e2: Exception) {
                        Log.w(TAG, "Cannot parse date: $dateStr")
                        continue
                    }
                }

                val minutesUntil = (matchTimeMs - nowMs) / 60000L

                // Gửi nếu trận trong khoảng 0..15 phút tới
                if (minutesUntil in 0..REMINDER_WINDOW_MINUTES) {
                    val homeTeam  = match.homeTeamName ?: "Home"
                    val awayTeam  = match.awayTeamName ?: "Away"

                    // Gửi FCM tới topic "match_reminder"
                    FcmV1Sender.sendMatchReminder(
                        context     = applicationContext,
                        matchId     = match.matchId,
                        homeTeam    = homeTeam,
                        awayTeam    = awayTeam,
                        minutesLeft = minutesUntil.toInt()
                    )
                    sentCount++
                    Log.d(TAG, "Reminder sent via FCM: $homeTeam vs $awayTeam in ${minutesUntil}min")
                }
            }

            Log.d(TAG, "Reminder check done. Sent=$sentCount")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Reminder worker error", e)
            Result.retry()
        }
    }
}
