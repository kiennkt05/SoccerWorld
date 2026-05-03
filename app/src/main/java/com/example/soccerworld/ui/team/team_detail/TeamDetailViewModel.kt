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
    val hasMoreMatches: Boolean = true
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
            0 -> loadSquad(teamId, isDetails = true)
            1 -> loadMatches(teamId)
            2 -> { /* Standings handled by existing LeagueTableScreen or similar */ }
            3 -> loadSquad(teamId, isDetails = false)
            4 -> loadTransfers(teamId)
        }
    }

    private fun loadSquad(teamId: String, isDetails: Boolean) {
        val stateToCheck = if (isDetails) _uiState.value.detailsState else _uiState.value.squadState
        if (stateToCheck is TabState.Success) return
        
        viewModelScope.launch {
            if (isDetails) _uiState.update { it.copy(detailsState = TabState.Loading) }
            else _uiState.update { it.copy(squadState = TabState.Loading) }

            when (val result = repository.getAllPlayersOfTeam(teamId)) {
                is DataResult.Success -> {
                    _uiState.update { 
                        if (isDetails) {
                            it.copy(
                                detailsState = TabState.Success(result.data),
                                teamName = result.data.name ?: it.teamName,
                                teamCrest = result.data.crest ?: it.teamCrest
                            )
                        } else {
                            it.copy(
                                squadState = TabState.Success(result.data),
                                teamName = result.data.name ?: it.teamName,
                                teamCrest = result.data.crest ?: it.teamCrest
                            )
                        }
                    }
                }
                is DataResult.Error -> {
                    _uiState.update { 
                        if (isDetails) it.copy(detailsState = TabState.Error(result.message ?: "Error"))
                        else it.copy(squadState = TabState.Error(result.message ?: "Error"))
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
            when (val result = repository.getTeamMatches(teamId, 1, isResults = true)) {
                is DataResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            matchesState = TabState.Success(result.data),
                            matchesPage = 1,
                            hasMoreMatches = result.data.isNotEmpty()
                        ) 
                    }
                }
                is DataResult.Error -> _uiState.update { it.copy(matchesState = TabState.Error(result.message ?: "Error")) }
                else -> {}
            }
        }
    }
}
