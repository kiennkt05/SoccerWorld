package com.example.soccerworld.ui.fixture

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.model.fixture.Matche
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TournamentInfo(
    val id: String,
    val name: String,
    val emblemUrl: String? = null,
    val areaName: String? = null,
    val areaFlag: String? = null
)

data class FixtureUiState(
    val isLoading: Boolean = true,
    val fixtureList: List<Matche> = emptyList(),
    val tournamentGroups: Map<String, Map<TournamentInfo, List<Matche>>> = emptyMap(),
    val expandedTournaments: Set<TournamentInfo> = emptySet(),
    val selectedTab: String = "",
    val availableTabs: List<String> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val hasLiveMatches: Boolean = false,
    val error: String? = null,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true
)

private data class FixturePresentation(
    val fixtureList: List<Matche>,
    val tournamentGroups: Map<String, Map<TournamentInfo, List<Matche>>>,
    val availableTabs: List<String>,
    val selectedTab: String,
    val hasLiveMatches: Boolean,
    val expandedTournaments: Set<TournamentInfo>
)

class FixtureViewModel(private val repository: FootballRepository) : ViewModel() {
    private companion object {
        private const val MAX_TOTAL_PAGES = 2
    }

    private val _uiState = MutableStateFlow(FixtureUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeFavorites()
        getAllFixtureOfLeague()
    }

    fun getAllFixtureOfLeague(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentPage = 1, hasMorePages = true) }
            val leagueId = repository.getSelectedLeagueId()

            when (val result = repository.getAllFixtureOfLeague(leagueId = leagueId, forceRefresh = forceRefresh)) {
                is DataResult.Success -> {
                    val presentation = withContext(Dispatchers.Default) {
                        buildPresentation(result.data.matches.orEmpty(), _uiState.value.selectedTab)
                    }

                    Log.d("FixtureViewModel", "Fixtures refreshed count=${presentation.fixtureList.size} tabs=${presentation.availableTabs.size}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            fixtureList = presentation.fixtureList,
                            tournamentGroups = presentation.tournamentGroups,
                            expandedTournaments = if (it.expandedTournaments.isEmpty()) presentation.expandedTournaments else it.expandedTournaments,
                            selectedTab = presentation.selectedTab,
                            availableTabs = presentation.availableTabs,
                            hasLiveMatches = presentation.hasLiveMatches,
                            hasMorePages = MAX_TOTAL_PAGES > 1
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
        _uiState.update { it.copy(selectedTab = tab, currentPage = 1, hasMorePages = true) }
    }

    fun toggleFavorite(match: Matche) {
        viewModelScope.launch {
            repository.toggleFavorite(match)
        }
    }

    fun toggleTournamentExpanded(tournamentInfo: TournamentInfo) {
        _uiState.update { state ->
            val expanded = state.expandedTournaments.toMutableSet()
            if (expanded.contains(tournamentInfo)) {
                expanded.remove(tournamentInfo)
            } else {
                expanded.add(tournamentInfo)
            }
            state.copy(expandedTournaments = expanded)
        }
    }

    private var lastLoadMoreTime = 0L
    private val LOAD_MORE_DEBOUNCE_MS = 500L

    fun loadMoreMatches() {
        val now = System.currentTimeMillis()
        if (now - lastLoadMoreTime < LOAD_MORE_DEBOUNCE_MS) return
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMorePages) return
        if (_uiState.value.currentPage >= MAX_TOTAL_PAGES) {
            _uiState.update { it.copy(hasMorePages = false) }
            return
        }
        
        lastLoadMoreTime = now
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val leagueId = repository.getSelectedLeagueId()
            val nextPage = _uiState.value.currentPage + 1
            if (nextPage > MAX_TOTAL_PAGES) {
                _uiState.update { it.copy(isLoadingMore = false, hasMorePages = false) }
                return@launch
            }
            
            when (val result = repository.loadMoreFixtures(leagueId, nextPage)) {
                is DataResult.Success -> {
                    val newMatches = result.data
                    if (newMatches.isEmpty()) {
                        _uiState.update { it.copy(isLoadingMore = false, hasMorePages = false) }
                        return@launch
                    }
                    
                    val presentation = withContext(Dispatchers.Default) {
                        buildPresentation(_uiState.value.fixtureList + newMatches, _uiState.value.selectedTab)
                    }
                    
                    Log.d("FixtureViewModel", "Loaded more: +${newMatches.size} matches, page=$nextPage")
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            fixtureList = presentation.fixtureList,
                            tournamentGroups = presentation.tournamentGroups,
                            currentPage = nextPage,
                            hasMorePages = nextPage < MAX_TOTAL_PAGES
                        )
                    }
                }
                is DataResult.Error -> {
                    Log.e("FixtureViewModel", "Error loading more: ${result.message}")
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
                DataResult.Loading -> {
                    _uiState.update { it.copy(isLoadingMore = true) }
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavorites().collect { favorites ->
                _uiState.update { it.copy(favoriteIds = favorites.map { fav -> fav.matchId }.toSet()) }
            }
        }
    }

    private fun buildPresentation(
        sourceMatches: List<Matche>,
        currentSelectedTab: String
    ): FixturePresentation {
        val matches = sourceMatches.distinctBy { it.id }.sortedByDescending { it.utcDate ?: "" }
        val groups = buildTournamentGroups(matches)
        val tabs = groups.keys.toList()
        val selectedTab = currentSelectedTab.takeIf { it in tabs } ?: tabs.firstOrNull().orEmpty()
        val hasLive = matches.any { it.status == "IN_PLAY" || it.status == "PAUSED" }
        val allTournaments = groups.values.flatMap { it.keys }.toSet()

        return FixturePresentation(
            fixtureList = matches,
            tournamentGroups = groups,
            availableTabs = tabs,
            selectedTab = selectedTab,
            hasLiveMatches = hasLive,
            expandedTournaments = allTournaments
        )
    }

    private fun buildTournamentGroups(matches: List<Matche>): Map<String, Map<TournamentInfo, List<Matche>>> {
        // Group by stage first (IN_PLAY, SCHEDULED, FINISHED)
        val groupedByStage = matches.groupBy { normalizeStage(it.status ?: it.stage) }
        
        // Sort stages by priority
        val orderedStageKeys = groupedByStage.keys.sortedWith(compareBy { stagePriority(it) })
        
        // For each stage, group by tournament
        return orderedStageKeys.associateWith { stage ->
            val stageMatches = groupedByStage[stage].orEmpty()
            
            // Group by tournament
            stageMatches.groupBy { 
                TournamentInfo(
                    id = it.competition?.code ?: "Unknown",
                    name = it.competition?.name ?: "Unknown League",
                    emblemUrl = it.competition?.emblem,
                    areaName = it.area?.name,
                    areaFlag = it.area?.flag
                )
            }
            // Matches are already sorted by utcDate from parent, no need to re-sort
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

}
