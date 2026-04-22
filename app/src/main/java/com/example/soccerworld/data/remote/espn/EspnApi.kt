package com.example.soccerworld.data.remote.espn

import com.example.soccerworld.data.remote.espn.dto.EspnScoreboardDto
import com.example.soccerworld.data.remote.espn.dto.EspnSummaryDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EspnApi {
    @GET("{league}/scoreboard")
    suspend fun getScoreboard(
        @Path("league") league: String,
        @Query("dates") date: String
    ): EspnScoreboardDto

    @GET("{league}/summary")
    suspend fun getSummary(
        @Path("league") league: String,
        @Query("event") eventId: String
    ): EspnSummaryDto
}
