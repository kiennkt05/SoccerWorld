package com.example.soccerworld.ui.team.team_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.remote.flashlive.TransferData
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.model.player.PlayerResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class TeamDetailUiState(
    val teamId: String = "",
    val teamName: String = "",
    val teamCrest: String? = null,
    val selectedTab: Int = 0,
    val detailsState: TabState<PlayerResponse> = TabState.Idle,
    val squadState: TabState<PlayerResponse> = TabState.Idle,
    val transfersState: TabState<List<TransferData>> = TabState.Idle,
    val matchesState: TabState<List<Matche>> = TabState.Idle,
    val matchesPage: Int = 1,
    val hasMoreMatches: Boolean = true,
    val isFavorite: Boolean = false,
    val favoriteMatchIds: Set<String> = emptySet(),
    val actualStageId: String? = null,
    val actualSeasonId: String? = null,
    val resolvedLeagueName: String? = null,
    val resolvedLeagues: List<com.example.soccerworld.util.FlashLiveLeague> = emptyList()
)

sealed class TabState<out T> {
    object Idle : TabState<Nothing>()
    object Loading : TabState<Nothing>()
    data class Success<T>(val data: T) : TabState<T>()
    data class Error(val message: String) : TabState<Nothing>()
}

class TeamDetailViewModel(private val repository: FootballRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun initTeam(teamId: String) {
        if (_uiState.value.teamId != teamId) {
            _uiState.update { TeamDetailUiState(teamId = teamId) }
            loadTab(0)
            observeFavoriteStatus(teamId)
            observeFavoriteMatches()
        }
    }

    private fun observeFavoriteMatches() {
        viewModelScope.launch {
            repository.observeFavorites().collect { favorites ->
                _uiState.update { state -> 
                    state.copy(favoriteMatchIds = favorites.map { it.matchId }.toSet()) 
                }
            }
        }
    }

    private fun observeFavoriteStatus(teamId: String) {
        viewModelScope.launch {
            repository.observeIsFavoriteTeam(teamId).collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value
        val teamId = state.teamId
        if (teamId.isEmpty()) return
        viewModelScope.launch {
            repository.toggleFavoriteTeam(
                teamId = teamId,
                name = state.teamName,
                logoUrl = state.teamCrest,
                country = null
            )
        }
    }

    fun toggleFavoriteMatch(match: Matche) {
        viewModelScope.launch {
            repository.toggleFavorite(match)
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        loadTab(index)
    }

    private fun loadTab(index: Int) {
        val teamId = _uiState.value.teamId
        if (teamId.isEmpty()) return

        when (index) {
            0 -> loadSquad(teamId, isDetails = false)
            1 -> loadMatches(teamId)
            2 -> { /* Standings handled by existing LeagueTableScreen or similar */ }
            3 -> loadTransfers(teamId)
        }
    }

    private fun loadSquad(teamId: String, isDetails: Boolean, forceRefresh: Boolean = false) {
        val stateToCheck = if (isDetails) _uiState.value.detailsState else _uiState.value.squadState
        if (stateToCheck is TabState.Success && !forceRefresh) return
        
        viewModelScope.launch {
            if (isDetails) _uiState.update { it.copy(detailsState = TabState.Loading) }
            else _uiState.update { it.copy(squadState = TabState.Loading) }

            when (val result = repository.getAllPlayersOfTeam(teamId, forceRefresh)) {
                is DataResult.Success -> {
                    val playerResponse = result.data
                    val fromCache = result.fromCache
                    
                    var stageId = playerResponse.actualStageId
                    var seasonId = playerResponse.actualSeasonId
                    
                    // 1. Search in local database cache
                    val localLeagues = repository.findLeaguesForTeam(teamId)
                    
                    var resolvedStageId = stageId ?: localLeagues.firstOrNull()?.stageId
                    var resolvedSeasonId = seasonId ?: localLeagues.firstOrNull()?.seasonId
                    
                    // 2. If still null and it was cached, force a refresh from network to get the fresh data
                    if (resolvedStageId == null && fromCache) {
                        loadSquad(teamId, isDetails, forceRefresh = true)
                        return@launch
                    }
                    
                    val apiLeague = stageId?.let { sId ->
                        com.example.soccerworld.util.Constant.FLASHLIVE_LEAGUES.values.firstOrNull {
                            it.stageId == sId || it.allStageIds.contains(sId)
                        }
                    }
                    
                    val mergedLeagues = (listOfNotNull(apiLeague) + localLeagues).distinct()
                    val domesticCodes = listOf("PL", "PD", "BL1", "SA", "FL1")
                    val sortedLeagues = mergedLeagues.sortedWith(compareBy { league ->
                        val key = com.example.soccerworld.util.Constant.FLASHLIVE_LEAGUES.entries.firstOrNull { it.value == league }?.key
                        val index = domesticCodes.indexOf(key)
                        if (index != -1) index else domesticCodes.size + 1
                    })
                    
                    // The first league in sorted list is the primary/domestic league
                    val primaryLeague = sortedLeagues.firstOrNull()
                    val resolvedName = primaryLeague?.name ?: playerResponse.area?.name
                    
                    if (resolvedStageId == null) {
                        resolvedStageId = primaryLeague?.stageId
                        resolvedSeasonId = primaryLeague?.seasonId
                    }
                    
                    _uiState.update { 
                        val cleanedName = (playerResponse.name ?: it.teamName).replace("*", "").trim()
                        val updatedResponse = playerResponse.copy(
                            actualStageId = resolvedStageId,
                            actualSeasonId = resolvedSeasonId
                        )
                        if (isDetails) {
                            it.copy(
                                detailsState = TabState.Success(updatedResponse),
                                teamName = cleanedName,
                                teamCrest = playerResponse.crest ?: it.teamCrest,
                                actualStageId = resolvedStageId,
                                actualSeasonId = resolvedSeasonId,
                                resolvedLeagueName = resolvedName,
                                resolvedLeagues = sortedLeagues
                            )
                        } else {
                            it.copy(
                                squadState = TabState.Success(updatedResponse),
                                teamName = cleanedName,
                                teamCrest = playerResponse.crest ?: it.teamCrest,
                                actualStageId = resolvedStageId,
                                actualSeasonId = resolvedSeasonId,
                                resolvedLeagueName = resolvedName,
                                resolvedLeagues = sortedLeagues
                            )
                        }
                    }
                }
                is DataResult.Error -> {
                    // Try to resolve locally as fallback
                    val localLeagues = repository.findLeaguesForTeam(teamId)
                    if (localLeagues.isNotEmpty()) {
                        val primaryLeague = localLeagues.first()
                        val stageId = primaryLeague.stageId
                        val seasonId = primaryLeague.seasonId
                        val resolvedName = primaryLeague.name
                        _uiState.update {
                            val dummyResponse = PlayerResponse(actualStageId = stageId, actualSeasonId = seasonId)
                            if (isDetails) {
                                it.copy(
                                    detailsState = TabState.Success(dummyResponse),
                                    actualStageId = stageId,
                                    actualSeasonId = seasonId,
                                    resolvedLeagueName = resolvedName,
                                    resolvedLeagues = localLeagues
                                )
                            } else {
                                it.copy(
                                    squadState = TabState.Success(dummyResponse),
                                    actualStageId = stageId,
                                    actualSeasonId = seasonId,
                                    resolvedLeagueName = resolvedName,
                                    resolvedLeagues = localLeagues
                                )
                            }
                        }
                    } else {
                        _uiState.update { 
                            if (isDetails) it.copy(detailsState = TabState.Error(result.message ?: "Error"))
                            else it.copy(squadState = TabState.Error(result.message ?: "Error"))
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun loadTransfers(teamId: String) {
        if (_uiState.value.transfersState is TabState.Success) return
        viewModelScope.launch {
            _uiState.update { it.copy(transfersState = TabState.Loading) }
            when (val result = repository.getTeamTransfers(teamId)) {
                is DataResult.Success -> _uiState.update { it.copy(transfersState = TabState.Success(result.data)) }
                is DataResult.Error -> _uiState.update { it.copy(transfersState = TabState.Error(result.message ?: "Error")) }
                else -> {}
            }
        }
    }

    fun loadMoreMatches() {
        val teamId = _uiState.value.teamId
        val currentPage = _uiState.value.matchesPage
        if (!_uiState.value.hasMoreMatches || _uiState.value.matchesState is TabState.Loading) return
        
        viewModelScope.launch {
            val nextPage = currentPage + 1
            when (val result = repository.getTeamMatches(teamId, nextPage, isResults = true)) {
                is DataResult.Success -> {
                    val currentList = (_uiState.value.matchesState as? TabState.Success)?.data ?: emptyList()
                    val newMatches = result.data
                    _uiState.update { 
                        it.copy(
                            matchesState = TabState.Success(currentList + newMatches),
                            matchesPage = nextPage,
                            hasMoreMatches = newMatches.isNotEmpty()
                        )
                    }
                }
                else -> {}
            }
        }
    }

    private fun loadMatches(teamId: String) {
        if (_uiState.value.matchesState is TabState.Success) return
        viewModelScope.launch {
            _uiState.update { it.copy(matchesState = TabState.Loading) }
            
            val resultsResult = repository.getTeamMatches(teamId, 1, isResults = true)
            val fixturesResult = repository.getTeamMatches(teamId, 1, isResults = false)
            
            val mergedMatches = mutableListOf<Matche>()
            var hasSuccess = false
            var errorMsg = "Error loading matches"

            if (resultsResult is DataResult.Success) {
                mergedMatches.addAll(resultsResult.data)
                hasSuccess = true
            } else if (resultsResult is DataResult.Error) {
                errorMsg = resultsResult.message ?: errorMsg
            }

            if (fixturesResult is DataResult.Success) {
                mergedMatches.addAll(fixturesResult.data)
                hasSuccess = true
            } else if (fixturesResult is DataResult.Error) {
                errorMsg = fixturesResult.message ?: errorMsg
            }

            if (hasSuccess) {
                _uiState.update { 
                    it.copy(
                        matchesState = TabState.Success(mergedMatches.distinctBy { match -> match.id }),
                        matchesPage = 1,
                        hasMoreMatches = false
                    ) 
                }
            } else {
                _uiState.update { it.copy(matchesState = TabState.Error(errorMsg)) }
            }
        }
    }
}
