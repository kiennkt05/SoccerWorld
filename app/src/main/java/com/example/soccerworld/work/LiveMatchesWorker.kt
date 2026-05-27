package com.example.soccerworld.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.util.FcmV1Sender
import com.example.soccerworld.util.Injection

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
        val repository = Injection.provideFootballRepository(applicationContext)
        val leagueId   = repository.getSelectedLeagueId()

        // ── 1. Kiểm tra trận đang live ────────────────────────────────────────
        val liveResult = repository.getAllFixtureOfLeague(
            leagueId     = leagueId,
            status       = "IN_PLAY",
            forceRefresh = true
        )

        when (liveResult) {
            is DataResult.Success -> {
                val liveMatches = liveResult.data.matches ?: emptyList()
                val liveCount   = liveMatches.size
                Log.d(TAG, "Live polling: liveCount=$liveCount")

                val prefs = applicationContext
                    .getSharedPreferences("live_score_cache", Context.MODE_PRIVATE)

                for (match in liveMatches) {
                    val matchId      = match.id ?: continue
                    val homeTeam     = match.homeTeam?.shortName ?: match.homeTeam?.name ?: "Home"
                    val awayTeam     = match.awayTeam?.shortName ?: match.awayTeam?.name ?: "Away"
                    val homeScore    = match.score?.fullTime?.home ?: 0
                    val awayScore    = match.score?.fullTime?.away ?: 0
                    val currentScore = "$homeScore-$awayScore"
                    val cachedScore  = prefs.getString(scoreKey(matchId), null)

                    // Tỷ số thay đổi → gửi FCM live score
                    if (cachedScore != null && cachedScore != currentScore) {
                        Log.d(TAG, "Score changed: $matchId | $cachedScore → $currentScore")
                        FcmV1Sender.sendLiveScore(
                            context   = applicationContext,
                            matchId   = matchId,
                            homeTeam  = homeTeam,
                            awayTeam  = awayTeam,
                            homeScore = homeScore,
                            awayScore = awayScore
                        )
                    }

                    // Cập nhật cache
                    prefs.edit()
                        .putString(scoreKey(matchId), currentScore)
                        .putString(statusKey(matchId), match.status ?: "")
                        .apply()
                }

                if (liveCount > 0) LivePollingScheduler.start(applicationContext)
                else               LivePollingScheduler.stop(applicationContext)
            }
            is DataResult.Error -> {
                Log.e(TAG, "Live polling failed: ${liveResult.message}")
                return Result.retry()
            }
            DataResult.Loading -> return Result.retry()
        }

        // ── 2. Kiểm tra trận vừa kết thúc (FINISHED) ─────────────────────────
        checkFinishedMatches(repository, leagueId)

        return Result.success()
    }

    private suspend fun checkFinishedMatches(
        repository: com.example.soccerworld.data.FootballRepository,
        leagueId: String
    ) {
        val result = repository.getAllFixtureOfLeague(
            leagueId     = leagueId,
            status       = "FINISHED",
            forceRefresh = true
        )
        if (result !is DataResult.Success) return

        val prefs = applicationContext
            .getSharedPreferences("live_score_cache", Context.MODE_PRIVATE)

        for (match in result.data.matches ?: return) {
            val matchId      = match.id ?: continue
            val cachedStatus = prefs.getString(statusKey(matchId), null)

            // Chỉ gửi nếu trước đó đang live
            if (cachedStatus == "IN_PLAY" || cachedStatus == "PAUSED" || cachedStatus == "HALF_TIME") {
                val homeTeam  = match.homeTeam?.shortName ?: match.homeTeam?.name ?: "Home"
                val awayTeam  = match.awayTeam?.shortName ?: match.awayTeam?.name ?: "Away"
                val homeScore = match.score?.fullTime?.home ?: 0
                val awayScore = match.score?.fullTime?.away ?: 0

                Log.d(TAG, "Match finished: $matchId | $homeTeam $homeScore-$awayScore $awayTeam")

                FcmV1Sender.sendMatchResult(
                    context   = applicationContext,
                    matchId   = matchId,
                    homeTeam  = homeTeam,
                    awayTeam  = awayTeam,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    winner    = match.score?.winner
                )

                // Xoá cache sau khi gửi kết quả
                prefs.edit()
                    .remove(scoreKey(matchId))
                    .remove(statusKey(matchId))
                    .apply()
            }
        }
    }
}
