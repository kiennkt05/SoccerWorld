package com.example.soccerworld.data.cache

import com.example.soccerworld.util.Constant
import java.util.concurrent.TimeUnit

object CacheTtl {
    val CACHE_WINDOW_MS: Long = TimeUnit.DAYS.toMillis(Constant.DAYS_TO_UPDATE.toLong())
    const val ESPN_ENRICHMENT_LIVE_MS = 60_000L
    const val ESPN_ENRICHMENT_FINISHED_MS = 86_400_000L
    const val ID_BRIDGE_MS = 604_800_000L
}
