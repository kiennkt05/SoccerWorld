package com.example.soccerworld.data.repository

import com.example.soccerworld.data.local.FootballDao
import com.example.soccerworld.data.local.entity.PlayerMediaEntity
import com.example.soccerworld.data.local.entity.TeamMediaEntity
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.model.ErrorType
import com.example.soccerworld.data.remote.sportsdb.SportsDbApi
import retrofit2.HttpException
import java.io.IOException

class MediaRepository(
    private val sportsDbApi: SportsDbApi,
    private val footballDao: FootballDao
) {
    suspend fun resolveTeamMedia(teamId: Int?, teamName: String?, fallbackCrest: String?): DataResult<TeamMediaEntity?> {
        if (teamId == null || teamName.isNullOrBlank()) {
            return DataResult.Success(null, fromCache = true)
        }

        val local = footballDao.getTeamMedia(teamId)
        if (local != null && !local.badgeUrl.isNullOrBlank()) {
            return DataResult.Success(local, fromCache = true)
        }

        return try {
            val remoteTeams = sportsDbApi.searchTeams(teamName).teams
            val remote = remoteTeams
                ?.firstOrNull { it.strTeam.equals(teamName, ignoreCase = true) }
                ?: remoteTeams?.firstOrNull()

            val entity = TeamMediaEntity(
                footballDataTeamId = teamId,
                teamName = teamName,
                badgeUrl = remote?.strBadge ?: fallbackCrest,
                bannerUrl = remote?.strBanner,
                lastUpdated = System.currentTimeMillis()
            )
            footballDao.insertTeamMedia(entity)
            DataResult.Success(entity)
        } catch (io: IOException) {
            DataResult.Error(ErrorType.MEDIA_FAIL, io.message)
        } catch (http: HttpException) {
            DataResult.Error(ErrorType.MEDIA_FAIL, http.message())
        } catch (e: Exception) {
            DataResult.Error(ErrorType.MEDIA_FAIL, e.message)
        }
    }

    suspend fun resolvePlayerMedia(playerId: Int?, playerName: String?): DataResult<PlayerMediaEntity?> {
        if (playerId == null || playerName.isNullOrBlank()) {
            return DataResult.Success(null, fromCache = true)
        }

        val local = footballDao.getPlayerMedia(playerId)
        if (local != null && (!local.thumbUrl.isNullOrBlank() || !local.cutoutUrl.isNullOrBlank())) {
            return DataResult.Success(local, fromCache = true)
        }

        return try {
            val remotePlayers = sportsDbApi.searchPlayers(playerName).player
            val remote = remotePlayers
                ?.firstOrNull { it.strPlayer.equals(playerName, ignoreCase = true) }
                ?: remotePlayers?.firstOrNull()

            val entity = PlayerMediaEntity(
                footballDataPlayerId = playerId,
                playerName = playerName,
                thumbUrl = remote?.strThumb,
                cutoutUrl = remote?.strCutout,
                lastUpdated = System.currentTimeMillis()
            )
            footballDao.insertPlayerMedia(entity)
            DataResult.Success(entity)
        } catch (io: IOException) {
            DataResult.Error(ErrorType.MEDIA_FAIL, io.message)
        } catch (http: HttpException) {
            DataResult.Error(ErrorType.MEDIA_FAIL, http.message())
        } catch (e: Exception) {
            DataResult.Error(ErrorType.MEDIA_FAIL, e.message)
        }
    }
}
