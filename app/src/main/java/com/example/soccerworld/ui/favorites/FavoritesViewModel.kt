package com.example.soccerworld.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.model.fixture.AwayTeam
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.model.fixture.HomeTeam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val matches: List<Matche> = emptyList()
)

class FavoritesViewModel(
    private val repository: FootballRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeFavorites().collect { favorites ->
                val mapped = favorites.map { fav ->
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
                _uiState.update { it.copy(isLoading = false, matches = mapped) }
            }
        }
    }
}
