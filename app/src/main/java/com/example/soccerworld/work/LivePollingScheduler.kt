package com.example.soccerworld.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object LivePollingScheduler {

    fun startForMatchId(context: Context, matchId: String, delayMs: Long) {
        val request = OneTimeWorkRequestBuilder<SingleMatchWorker>()
            .setInputData(Data.Builder().putString("matchId", matchId).build())
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
            
        val workName = "poll_$matchId"
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun stopForMatchId(context: Context, matchId: String) {
        val workName = "poll_$matchId"
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }
}
