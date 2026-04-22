package com.example.soccerworld.data.remote

import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random

object RetryPolicy {
    suspend fun <T> executeWithBackoff(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 400L,
        maxDelayMs: Long = 3_000L,
        block: suspend () -> T
    ): T {
        var attempt = 0
        var currentDelay = initialDelayMs
        var lastError: Throwable? = null

        while (attempt < maxAttempts) {
            try {
                return block()
            } catch (e: Throwable) {
                lastError = e
                val shouldRetry = when (e) {
                    is IOException -> true
                    is HttpException -> e.code() in setOf(403, 429, 503)
                    else -> false
                }
                attempt++
                if (!shouldRetry || attempt >= maxAttempts) break
                val jitter = Random.nextLong(100L, 250L)
                delay(min(currentDelay + jitter, maxDelayMs))
                currentDelay = min(currentDelay * 2, maxDelayMs)
            }
        }

        throw (lastError ?: IllegalStateException("Retry policy failed without captured error"))
    }
}
