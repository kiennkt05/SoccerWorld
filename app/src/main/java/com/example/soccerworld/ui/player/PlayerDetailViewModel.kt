package com.example.soccerworld.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.remote.flashlive.PlayerData
import com.example.soccerworld.data.remote.flashlive.CareerTabBlock
import com.example.soccerworld.model.fixture.Matche
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class PlayerDetailUiState(
    val isLoadingMatches: Boolean = false,
    val matches: List<Matche> = emptyList(),
    val matchesError: String? = null,
    val isLoadingDetails: Boolean = false,
    val playerData: PlayerData? = null,
    val detailsError: String? = null,
    val isLoadingCareer: Boolean = false,
    val careerTabs: List<CareerTabBlock> = emptyList(),
    val careerError: String? = null,
    val isFavorite: Boolean = false
)

class PlayerDetailViewModel(private val repository: FootballRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerDetailUiState())
    val uiState = _uiState.asStateFlow()

    private fun observeFavoriteStatus(playerId: String) {
        viewModelScope.launch {
            repository.observeIsFavoritePlayer(playerId).collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun toggleFavorite(playerId: String, fallbackName: String, fallbackImageUrl: String?, fallbackNationality: String?) {
        val state = _uiState.value
        val pData = state.playerData
        viewModelScope.launch {
            repository.toggleFavoritePlayer(
                playerId = playerId,
                name = pData?.name ?: fallbackName,
                imageUrl = pData?.imagePath ?: fallbackImageUrl,
                nationality = pData?.countryName ?: fallbackNationality,
                position = pData?.typeName
            )
        }
    }

    fun loadPlayerDataAndCareer(playerId: String) {
        observeFavoriteStatus(playerId)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingDetails = true,
                    detailsError = null,
                    isLoadingCareer = true,
                    careerError = null
                )
            }

            val detailsDeferred = async { repository.getPlayerData(playerId) }
            val careerDeferred = async { repository.getPlayerCareer(playerId) }

            val detailsResult = detailsDeferred.await()
            val careerResult = careerDeferred.await()

            when (detailsResult) {
                is DataResult.Success -> {
                    val pData = detailsResult.data.data
                    _uiState.update {
                        it.copy(
                            isLoadingDetails = false,
                            playerData = pData
                        )
                    }
                    pData?.teamId?.let { tId ->
                        loadTeamMatches(tId)
                    }
                }
                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingDetails = false,
                            detailsError = detailsResult.message ?: "Failed to load player details"
                        )
                    }
                }
                else -> {}
            }

            when (careerResult) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingCareer = false,
                            careerTabs = careerResult.data.data.orEmpty()
                        )
                    }
                }
                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingCareer = false,
                            careerError = careerResult.message ?: "Failed to load career statistics"
                        )
                    }
                }
                else -> {}
            }
        }
    }

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
