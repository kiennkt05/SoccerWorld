package com.example.soccerworld.data.remote.sportsdb

import com.example.soccerworld.data.remote.sportsdb.dto.PlayerSearchResponseDto
import com.example.soccerworld.data.remote.sportsdb.dto.TeamSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SportsDbApi {

    @GET("searchteams.php")
    suspend fun searchTeams(
        @Query("t") teamName: String
    ): TeamSearchResponseDto

    @GET("searchplayers.php")
    suspend fun searchPlayers(
        @Query("p") playerName: String
    ): PlayerSearchResponseDto
}
