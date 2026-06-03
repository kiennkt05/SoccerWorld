package com.example.soccerworld.ui.fixture.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.remote.flashlive.HighlightItem
import com.example.soccerworld.model.matchdetail.MatchDetailAggregate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

data class MatchDetailUiState(
    val highlights: List<HighlightItem> = emptyList(),
    val isLoading: Boolean = true,
    val data: MatchDetailAggregate? = null,
    val error: String? = null
)

class MatchDetailViewModel(
    private val repository: FootballRepository
) : ViewModel() {
    private val tag = "MatchDetailVM"
    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var autoRefreshJob: kotlinx.coroutines.Job? = null

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun loadHighlights(fixtureId: String) {
        viewModelScope.launch {
            val result = repository.getEventHighlights(fixtureId)
            if (result is DataResult.Success) {
                _uiState.update { it.copy(highlights = result.data ?: emptyList()) }
            }
        }
    }

    fun refresh(fixtureId: String) = loadMatchDetail(fixtureId, forceRefresh = true)

    fun loadMatchDetail(fixtureId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            Log.d(tag, "Loading aggregate for fixtureId=$fixtureId")
            when (val result = repository.getMatchDetailAggregate(fixtureId, forceRefresh)) {
                is DataResult.Success -> {
                    Log.d(
                        tag,
                        "Loaded aggregate fixtureId=$fixtureId h2h=${result.data.h2h.size} " +
                            "events=${result.data.enrichment?.events?.size ?: 0} " +
                            "stats=${result.data.enrichment?.stats?.size ?: 0} " +
                            "lineups=${result.data.enrichment?.lineups?.size ?: 0}"
                    )
                    _uiState.update { it.copy(isLoading = false, data = result.data) }
                    
                    val status = result.data.core?.status ?: "FINISHED"
                    val isLive = status == "IN_PLAY" || status == "PAUSED" || status == "LIVE" || status == "HALFTIME"
                    
                    if (isLive && autoRefreshJob == null) {
                        startAutoRefresh(fixtureId)
                    } else if (!isLive && autoRefreshJob != null) {
                        stopAutoRefresh()
                    }
                }
                is DataResult.Error -> {
                    Log.e(tag, "Aggregate load failed fixtureId=$fixtureId type=${result.type} msg=${result.message}")
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to load match detail") }
                }
                DataResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun startAutoRefresh(fixtureId: String) {
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(30_000L)
                Log.d(tag, "Auto-refreshing match detail...")
                val result = repository.getMatchDetailAggregate(fixtureId, forceRefresh = true)
                if (result is DataResult.Success) {
                    _uiState.update { it.copy(data = result.data) }
                    val status = result.data.core?.status ?: "FINISHED"
                    val isLive = status == "IN_PLAY" || status == "PAUSED" || status == "LIVE" || status == "HALFTIME"
                    if (!isLive) {
                        stopAutoRefresh()
                    }
                }
            }
        }
    }

    private fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }
}
