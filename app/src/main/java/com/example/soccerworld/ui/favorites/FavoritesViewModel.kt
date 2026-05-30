package com.example.soccerworld.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.local.entity.FavoritePlayerEntity
import com.example.soccerworld.data.local.entity.FavoriteTeamEntity
import com.example.soccerworld.model.fixture.AwayTeam
import com.example.soccerworld.model.fixture.HomeTeam
import com.example.soccerworld.model.fixture.Matche
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
                repository.getAllFavoriteTeams(),
                repository.observeFavoritePlayers()
            ) { favorites, teams, players ->
                val mappedMatches = favorites.map { fav ->
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
                        ),
                        score = com.example.soccerworld.model.fixture.Score(
                            fullTime = com.example.soccerworld.model.fixture.FullTime(
                                home = fav.homeScore,
                                away = fav.awayScore
                            )
                        )
                    )
                }
                FavoritesUiState(isLoading = false, matches = mappedMatches, teams = teams, players = players)
            }.collect { state ->
                _uiState.value = state
            }
        }
        
        syncFavoriteMatches()
    }
    
    private fun syncFavoriteMatches() {
        viewModelScope.launch {
            repository.syncFavoriteMatches()
        }
    }

    fun toggleFavoriteTeam(teamId: String, name: String, logoUrl: String?, countryName: String?) {
        viewModelScope.launch {
            repository.toggleFavoriteTeam(teamId, name, logoUrl, countryName)
        }
    }

    fun toggleFavoritePlayer(playerId: String, name: String, imageUrl: String?, nationality: String?, position: String?) {
        viewModelScope.launch {
            repository.toggleFavoritePlayer(playerId, name, imageUrl, nationality, position)
        }
    }

    fun toggleFavoriteMatch(match: Matche) {
        viewModelScope.launch {
            repository.toggleFavorite(match)
        }
    }
}

