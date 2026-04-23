package com.example.soccerworld.ui.fixture

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.model.fixture.Matche
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FixtureUiState(
    val isLoading: Boolean = true,
    val fixtureList: List<Matche> = emptyList(),
    val stageRoundGroups: Map<String, Map<String, List<Matche>>> = emptyMap(),
    val selectedTab: String = "",
    val availableTabs: List<String> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val hasLiveMatches: Boolean = false,
    val error: String? = null
)

class FixtureViewModel(private val repository: FootballRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FixtureUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeFavorites()
        getAllFixtureOfLeague()
    }

    fun getAllFixtureOfLeague(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val leagueId = repository.getSelectedLeagueId()

            when (val result = repository.getAllFixtureOfLeague(leagueId = leagueId, forceRefresh = forceRefresh)) {
                is DataResult.Success -> {
                    val matches = result.data.matches.orEmpty().sortedByDescending { it.utcDate ?: "" }
                    val groups = buildStageRoundGroups(matches)
                    val tabs = groups.keys.toList()
                    val selectedTab = _uiState.value.selectedTab.takeIf { it in tabs } ?: tabs.firstOrNull().orEmpty()
                    val hasLive = matches.any { it.status == "IN_PLAY" || it.status == "PAUSED" }
                    Log.d("FixtureViewModel", "Fixtures refreshed count=${matches.size} tabs=${tabs.size}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            fixtureList = matches,
                            stageRoundGroups = groups,
                            selectedTab = selectedTab,
                            availableTabs = tabs,
                            hasLiveMatches = hasLive
                        )
                    }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Lỗi tải lịch thi đấu") }
                }
                DataResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFavorite(match: Matche) {
        viewModelScope.launch {
            repository.toggleFavorite(match)
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavorites().collect { favorites ->
                _uiState.update { it.copy(favoriteIds = favorites.map { fav -> fav.matchId }.toSet()) }
            }
        }
    }

    private fun buildStageRoundGroups(matches: List<Matche>): Map<String, Map<String, List<Matche>>> {
        val groupedByStage = matches.groupBy { normalizeStage(it.status ?: it.stage) }
        val orderedStageKeys = groupedByStage.keys.sortedWith(compareBy { stagePriority(it) })
        return orderedStageKeys.associateWith { stage ->
            val stageMatches = groupedByStage[stage].orEmpty()
            val groupedByRound = stageMatches.groupBy { roundLabel(it.group) }
            groupedByRound.keys
                .sortedWith(compareByDescending<String> { extractRoundNumber(it) ?: Int.MIN_VALUE }.thenByDescending { it })
                .associateWith { round -> groupedByRound[round].orEmpty().sortedByDescending { it.utcDate ?: "" } }
        }
    }

    private fun normalizeStage(stage: String?): String {
        return when (stage?.uppercase()) {
            "IN_PLAY", "LIVE", "PAUSED", "HALFTIME" -> "IN_PLAY"
            "FINISHED", "FT" -> "FINISHED"
            "SCHEDULED" -> "SCHEDULED"
            null, "" -> "UNKNOWN"
            else -> stage.uppercase()
        }
    }

    private fun stagePriority(stage: String): Int {
        return when (stage) {
            "IN_PLAY" -> 0
            "SCHEDULED" -> 1
            "FINISHED" -> 2
            else -> 3
        }
    }

    private fun roundLabel(groupValue: Any?): String {
        val raw = groupValue?.toString()?.trim().orEmpty()
        return raw.ifBlank { "Unknown Round" }
    }

    private fun extractRoundNumber(label: String): Int? {
        return Regex("(\\d+)").find(label)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }
}
