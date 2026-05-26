package com.example.soccerworld.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.model.fixture.Matche
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerDetailUiState(
    val isLoadingMatches: Boolean = false,
    val matches: List<Matche> = emptyList(),
    val matchesError: String? = null
)

class PlayerDetailViewModel(private val repository: FootballRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadTeamMatches(teamId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMatches = true, matchesError = null) }

            val resultsDeferred = async { repository.getTeamMatches(teamId, 1, true) }
            val fixturesDeferred = async { repository.getTeamMatches(teamId, 1, false) }

            val results = resultsDeferred.await()
            val fixtures = fixturesDeferred.await()

            val allMatches = mutableListOf<Matche>()
            if (results is DataResult.Success) allMatches += results.data
            if (fixtures is DataResult.Success) allMatches += fixtures.data

            if (allMatches.isEmpty()) {
                val errorMsg = when {
                    results is DataResult.Error -> results.message
                    fixtures is DataResult.Error -> fixtures.message
                    else -> "No matches found"
                }
                _uiState.update { it.copy(isLoadingMatches = false, matchesError = errorMsg) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoadingMatches = false,
                        matches = allMatches
                            .distinctBy { m -> m.id }
                            .sortedByDescending { m -> m.utcDate.orEmpty() }
                    )
                }
            }
        }
    }
}
