package com.example.soccerworld.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.local.entity.FavoriteTeamEntity
import com.example.soccerworld.data.local.entity.FavoritePlayerEntity
import com.example.soccerworld.model.fixture.AwayTeam
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.model.fixture.HomeTeam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val matches: List<Matche> = emptyList(),
    val teams: List<FavoriteTeamEntity> = emptyList(),
    val players: List<FavoritePlayerEntity> = emptyList()
)

class FavoritesViewModel(
    private val repository: FootballRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeFavorites(),
                repository.observeFavoriteTeams(),
                repository.observeFavoritePlayers()
            ) { favMatches, favTeams, favPlayers ->
                val mapped = favMatches.map { fav ->
                    Matche(
                        id = fav.matchId,
                        utcDate = fav.utcDate,
                        status = fav.status,
                        homeTeam = HomeTeam(
                            id = fav.homeTeamId,
                            name = fav.homeTeamName,
                            crest = fav.homeTeamCrest
                        ),
                        awayTeam = AwayTeam(
                            id = fav.awayTeamId,
                            name = fav.awayTeamName,
                            crest = fav.awayTeamCrest
                        )
                    )
                }
                Triple(mapped, favTeams, favPlayers)
            }.collect { (mappedMatches, favTeams, favPlayers) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        matches = mappedMatches,
                        teams = favTeams,
                        players = favPlayers
                    )
                }
            }
        }
    }

    fun toggleFavoriteMatch(match: Matche) {
        viewModelScope.launch {
            repository.toggleFavorite(match)
        }
    }

    fun toggleFavoriteTeam(teamId: String, name: String, logoUrl: String?) {
        viewModelScope.launch {
            repository.toggleFavoriteTeam(
                teamId = teamId,
                name = name,
                logoUrl = logoUrl,
                country = null
            )
        }
    }

    fun toggleFavoritePlayer(playerId: String, name: String, imageUrl: String?) {
        viewModelScope.launch {
            repository.toggleFavoritePlayer(
                playerId = playerId,
                name = name,
                imageUrl = imageUrl,
                nationality = null,
                position = null
            )
        }
    }
}
